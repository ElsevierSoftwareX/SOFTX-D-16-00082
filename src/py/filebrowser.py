#!/usr/bin/python
# made from tutorial http://wiki.wxpython.org/AnotherTutorial

"""
Copyright 2013 Ian Campbell, Kevin "Feng" Chen, Purdue University

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

import argparse
import atexit
import datetime
import errno
import functools
import inspect
import itertools
import json
import mimetools
import mimetypes
from   multiprocessing.connection import Client
from   multiprocessing.connection import Listener
import os
paramiko_available = True
try:
	import paramiko
except ImportError:
	paramiko_available = False
import pickle
import select
import shutil
import stat
import subprocess
import sys
import tempfile
import threading
import time
import urllib, urllib2
import wx
from   wx.lib.mixins.listctrl import ListCtrlAutoWidthMixin
import traceback

devnull = open(os.devnull, 'w')
normaloutputfile = devnull # change to sys.stdout to use print
actualoutputfile = sys.stdout

sys.stdout = normaloutputfile

# ===================
# === String "table"
# ===================

# Titles
TTL_OPEN                = ' Open'
TTL_SAVE                = ' Save'
TTL_NORMAL              = ' Hub Files'
TTL_NEW_FILE            = 'New File'
TTL_DELETE              = 'Delete'
TTL_COMPRESS            = 'Compress Files'
TTL_SFTP                = ' SFTP Connection'
TTL_IDATA               = ' iData Connection'
# Option help
HLP_DESCRIPTION         = 'Common filesystem access utility for hub workspace and tools'
HLP_MULTIPLE            = 'with open/save, allow selection of more than one item'
HLP_SUGGEST             = 'with save, set initial value of item name'
HLP_WORKSPACEDIR        = 'create top level entry for specified folder. Can be used multiple times'
HLP_WORKSPACEDESC       = 'describe a shortcut'
HLP_IDATAPROJECT        = 'specify a project for idata files'
HLP_IDATAPROJECTDESC    = 'description for idata project'
HLP_INSTRUCT            = 'display text above file list'
HLP_STAGE               = 'with daemon & open/save, locally store remote selections(s)'
HLP_OPEN                = 'act as "Open" dialog, return selected item(s)'
HLP_OPEN_REMOTE         = 'behavior when selecting a remote file while in "open" mode when not staged. Download will download the file to a temporary directory and return it. Disallowed will give the user an error. Allowed will return the remote path.'
HLP_SAVE                = 'act as "Save As" dialog, return specified item(s)'
HLP_FILES               = 'with open/save, prevent selection of folders'
HLP_FOLDERS             = 'with open/save, prevent selection of files'
HLP_DAEMON              = """start: wait as hidden background process
appear: show window, with save/open: return selected items
sync: copy locally staged files to their remote destinations
stop: end background process"""
HLP_HIDE_HOME           = "don't show the home directory"
HLP_HIDE_SESSION        = "don't show the session directory"
HLP_HIDE_SDATA          = "don't show the sdata directory"
HLP_HIDE_IDATA          = "don't show all of the idata projects. Projects created with --idataproject will still be shown"
HLP_FILTER              = "show only certain file extensions. Use this argument multiple times for multiple extensions"
# Warnings and errors
WRN_SELECT_SINGLE       = 'Please select a single item.'
WRN_SELECT_FILE         = 'Please select file(s) only.'
WRN_SELECT_FOLDER       = 'Please select folder(s) only.'
WRN_NOT_EXIST           = 'File(s) do not exist.'
ERR_NOT_FOUND           = 'ERROR: Cannot find item.'
ERR_PROB_REMOTE_XFER    = 'ERROR: Unable to complete transfer of files to remote location.'
# Prompts
PMT_OVERWRITE_FILE      = 'Overwrite the existing file?'
PMT_READY               = 'Ready'
PMT_NEW_FILE            = 'New file name:'
PMT_DELETE              = 'Delete the selected item(s)?'
PMT_ARCHIVE_NAME        = 'Archive file name:'
# Menus and menu items
MNU_FILE                = '&File'
ITM_FILE                = 'New File...'
ITM_FOLDER              = 'New Folder...'
ITM_EDIT                = 'Edit (Open)'
ITM_RENAME              = 'Rename...'
ITM_COMPRESS            = 'Compress (Zip)...'
ITM_EXTRACT             = 'Extract (Unzip)'
ITM_EXIT                = 'Exit'
MNU_EDIT                = '&Edit'
ITM_CUT                 = 'Cut'
ITM_COPY                = 'Copy'
ITM_PAST                = 'Paste'
ITM_DELETE              = 'Delete...'
MNU_VIEW                = '&View'
ITM_BACK                = 'Go Back'
ITM_UP                  = 'Go Up'
ITM_REFRESH             = 'Refresh'
ITM_HIDDEN              = 'Show Hidden'
ITM_SORTNAME            = 'Sort by Name'
ITM_SORTSIZE            = 'Sort by Size'
ITM_SORTDATE            = 'Sort by Date'
MNU_TRANSFER            = '&Transfer'
ITM_ULOAD               = 'Upload...'
ITM_DLOAD               = 'Download'
MNU_CONNECT             = '&Connect'
ITM_SFTP                = 'SFTP'
ITM_IDATA               = 'old iData'
# Labels (buttons, column headers, etc.)
LBL_BACK                = 'Back'
LBL_UP                  = 'Up'
LBL_REFRESH             = 'Refresh'
LBL_OPEN                = 'Open'
LBL_CANCEL              = 'Cancel'
LBL_SAVE                = 'Save'
LBL_NAME                = 'Name'
LBL_SIZE                = 'Size'
LBL_DATE                = 'Date'
LBL_BYTES               = ' Bytes'
LBL_HOST                = 'Hostname:'
LBL_USER                = 'Username:'
LBL_PASS                = 'Password:'
LBL_CONNECT             = 'Connect'
LBL_DATASET             = 'Select Dataset:'
# Status bar text
STS_STARTUP             = 'Starting up...'
STS_PASTE               = 'Working: Pasting '
STS_INTO                = ' into '

# ===================
# === Utility methods
# ===================

def enum(*enums):
	"""Create enumerated types"""
	actualenums = {}
	reverse = {}
	for i, e in enumerate(enums):
		actualenums[e] = i
		reverse[i] = e
	actualenums["reverse_mapping"] = reverse

	return type("Enum", (), actualenums)

# ==================================
# === Model classes - handle storage
# ==================================

# File system object
class Fso(object):
	'''Abstract base for file system object classes'''

	CACHED_METHODS       = ["listdir", "exists", "size", "mtime", "content"] # results cache for speed
	FIXED_METHODS        = ["isfile", "isdir", "get_parent", "get_parents"] # methods which always return the same results for the same item
	UPDATE_CACHE_METHODS = ["copy", "copy_from_local", "mkdir", "create", "remove"] # methods which might cause results of cached methods to change (for the file system)
	cache                = {}
	object_cache_times   = {"all":0}
	method_cache_times   = {}

	@classmethod
	def set_cache_time_all(cls):
		Fso.object_cache_times["all"] = time.time()

	def __init__(self, path):
		self.path = path

		if not self.__key() in Fso.cache:
			Fso.cache[self.__key()]             = {}
			Fso.method_cache_times[self.__key()] = {}

	def set_cache_time_obj(self):
		Fso.object_cache_times[self.fs()] = time.time()

	def __getattribute__(self,name):
		attr = object.__getattribute__(self, name)
		if hasattr(attr, '__call__'):
			if name in Fso.CACHED_METHODS:
				def newfunc(*args, **kwargs):
					if not name in Fso.cache[self.__key()]:
						Fso.cache[self.__key()][name] = attr(*args, **kwargs)
						Fso.method_cache_times[self.__key()][name] = time.time()
					else:
						fslastupdated = Fso.object_cache_times["all"]
						if self.fs() in Fso.object_cache_times:
							fslastupdated = max(Fso.object_cache_times[self.fs()], fslastupdated)
						if fslastupdated > Fso.method_cache_times[self.__key()][name]:
							Fso.cache[self.__key()][name] = attr(*args, **kwargs)
							Fso.method_cache_times[self.__key()][name] = time.time()
					return Fso.cache[self.__key()][name]
				return newfunc
			elif name in Fso.FIXED_METHODS:
				def newfunc(*args, **kwargs):
					if not name in Fso.cache[self.__key()]:
						Fso.cache[self.__key()][name] = attr(*args, **kwargs)
					return Fso.cache[self.__key()][name]
				return newfunc
			elif name in Fso.UPDATE_CACHE_METHODS:
				def newfunc(*args, **kwargs):
					result = attr(*args, **kwargs)
					self.set_cache_time_obj()
					for arg in args:
						if isinstance(arg, Fso):
							arg.set_cache_time_obj()
					for index in kwargs:
						value = kwargs[index]
						if isinstance(value, Fso):
							value.set_cache_time_obj()
					return result
				return newfunc
			else:
				return attr
		else:
			return attr

	def __key(self):
		path = self.path
		if path.endswith("/"):
			path = path[:-1]
		return (path,) + self.fs()

	def __hash(self):
		return hash(self.__key())

	def __eq__(self, obj):
		return isinstance(obj, Fso) and self.__key() == obj.__key()

	def join(self, path):
		return self.new_for_path(os.path.join(self.path, path))

	def normpath(self):
		return os.path.normpath(self.path)

	def name(self):
		return os.path.split(os.path.abspath(self.path))[1]

	def dirname(self):
		return self.new_for_path(os.path.dirname(self.path))

	def basename(self):
		return os.path.basename(self.path)

	def hidden(self):
		return self.basename()[0] == '.'

	def get_parents(self): # must be in correct order
		parent = self.get_parent()
		if parent == None:
			return []
		else:
			return [parent] + parent.get_parents()

	def ischild(self, parent, include=False):
		return (parent in self.get_parents() or (parent == self and include))

	def current(self):
		return str(self.path)

	def to_temp_local(self):
		return self

	def get_parent(self):
		parent = os.path.dirname(self.path)
		parent = self.new_for_path(parent)
		if parent == self:
			return None
		return parent

	def make_dirs(self):
		parents = self.get_parents()
		parents.reverse()
		for parent in parents:
			if not parent.exists():
				parent.mkdir()

	def multi_same_fs(self, paths):
		allfiles = [self] + paths
		sfs = True
		for f1, f2 in itertools.combinations(allfiles, 2):
			if not f1.same_fs(f2):
				sfs = False
				break
		return sfs

	def make_command(self, cmd, files):
		if self.multi_same_fs(files):
			files.reverse()
			allpaths = [os.path.relpath(f.path, self.path) for f in files]
			newcmd = []
			for item in cmd:
				if item == None:
					item = allpaths.pop()
				newcmd.append(item)
			return newcmd
		else:
			return None

	def can_extract(self):
		for ending, program in ctrl.extractors:
			if self.path.endswith(ending):
				return True
		return False

	def same_fs(self, otherpath):
		return self.fs() == otherpath.fs()

	def can_download(self):
		return self.isfile()

	def new_for_path(self, path):
		'''Create new object, of same type, using given path'''
		return None # Override this method

class LocalFso(Fso):
	def __init__(self, path):
		self.is_remote = False
		Fso.__init__(self, path)

	def listdir(self):
		files = os.listdir(self.path)
		return [self.join(f) for f in files]

	def isfile(self):
		return os.path.isfile(self.path)

	def isdir(self):
		return os.path.isdir(self.path)

	def new_for_path(self, path):
		return LocalFso(path)

	def mkdir(self):
		return os.mkdir(self.path)

	def __repr__(self):
		return 'LocalFso<' + self.path + '>'

	def exists(self):
		return os.path.exists(self.path)

	def create(self):
		f = open(self.path,'w')
		f.close()

	def can_edit(self):
		return os.access(self.path, os.W_OK)

	def can_command(self, display=False, local=False):
		return True

	def run_command(self, cmd, wait=True, thread=None, files=[], display=False, local=False):
		cmd = self.make_command(cmd, files)
		proc = subprocess.Popen(cmd, cwd=self.path, stdout=subprocess.PIPE)
		if wait:
			proc.wait()
		if thread:
			waitthread = threading.Thread(target=functools.partial(self.wait_for_command, proc, thread))
			waitthread.start()
			ctrl.waitingprocs.append(proc)

	def wait_for_command(self, proc, funct):
		proc.wait()
		wx.CallAfter(funct)

	def size(self):
		return os.path.getsize(self.path)

	def mtime(self):
		return os.path.getmtime(self.path)

	def fs(self):
		return ("Local",)

	def remove(self):
		if self.isfile():
			return os.remove(self.path)
		elif self.isdir():
			return shutil.rmtree(self.path)

	def copy(self, newpath):
		if self.same_fs(newpath):
			if self.isfile():
				shutil.copy(self.path, newpath.path)
			elif self.isdir():
				shutil.copytree(self.path, newpath.path)
		else:
			newpath.copy_from_local(self)

	def upload(self, refreshcommand):
		return self.run_command(["importfile"], thread=refreshcommand, wait=False, local=True)

	def download(self, afile, delete=False):
		'''Download a file in my path'''
		cmd = ['exportfile']
		if delete:
			cmd.append('--delete')
		cmd.append(None)
		self.run_command(cmd, wait=True, files=[afile], local=True)

class RemoteFso(Fso):
	def __init__(self, path):
		self.is_remote = True
		Fso.__init__(self, path)

	def to_temp_local(self):
		if self.is_remote:
			tempdir = model.tempdir
			newpath = tempdir.join(self.basename())
			self.copy(newpath)
			return newpath
		else:
			return self

	def upload(self, refreshfunct):
		# TODO Get this working. Requires run_command() capture & return results.
		raise NotImplementedError
		#files = model.tempdir.upload()
		#for afile in files:
		#	tmp = LocalFso(afile)
		#	item.copy_from_local(tmp)
		#	tmp.remove()

	def download(self, item):
		local  = item.to_temp_local()
		parent = local.get_parent()
		parent.download(local,delete=True)

class SftpFso(RemoteFso):
	def __init__(self, sftp, path, servername):
		self.sftp = sftp
		self.servername = servername
		RemoteFso.__init__(self, path)

	def listdir(self):
		files = self.sftp.listdir(self.path)
		return [self.join(f) for f in files]

	def __repr__(self):
		return "SftpFso<" + self.servername + ":" + self.path + ">"

	def remote_path(self):
		return "sftp://" + self.servername + self.path

	def isfile(self):
		if self.exists():
			return stat.S_ISREG(self.sftp.stat(self.path).st_mode)

	def isdir(self):
		if self.exists():
			return stat.S_ISDIR(self.sftp.stat(self.path).st_mode)

	def mtime(self):
		if self.exists():
			return self.sftp.stat(self.path).st_mtime

	def size(self):
		if self.exists():
			return self.sftp.stat(self.path).st_size

	def new_for_path(self, path):
		return SftpFso(self.sftp, path, self.servername)

	def copy(self, newpath):
		if self.isdir():
			newpath.mkdir()
			for content in self.listdir():
				content.copy(newpath.join(content.basename()))
		else:
			if self.same_fs(newpath):
				fo = self.sftp.open(self.path)
				newpath.sftp.putfo(fo, newpath.path)
				fo.close()
			elif not newpath.is_remote:
				self.sftp.get(self.path, newpath.path)
			else:
				localself = self.to_temp_local()
				localself.copy(newpath)
				localself.remove()

	def copy_from_local(self, path):
		if path.isdir():
			self.mkdir()
			for content in path.listdir():
				self.join(content.basename()).copyfrom(content)
		else:
			self.sftp.put(path.path, self.path)

	def exists(self):
		try:
			self.sftp.stat(self.path)
		except IOError, e:
			if e.errno == errno.ENOENT:
				return False
			raise
		else:
			return True

	def create(self):
		fo = self.sftp.open(self.path, "w")
		fo.close()

	def remove(self):
		if self.isfile():
			return self.sftp.remove(self.path)
		elif self.isdir():
			for content in self.listdir():
				content.remove()
			return self.sftp.rmdir(self.path)

	def mkdir(self):
		self.sftp.mkdir(self.path)

	def can_edit(self):
		uid, gid = self.sftp.uid, self.sftp.gid
		s = self.sftp.stat(self.path)
		m = s.st_mode
		if s.st_uid == uid and stat.S_IWUSR & m:
			return True
		if s.st_gid == gid and stat.S_IWGRP & m:
			return True
		if stat.S_IWOTH & m:
			return True
		return False

	def fs(self):
		return (self.sftp, )

	def can_command(self, display=False, local=False):
		if not display and not local:
			return True
		return False

	def run_command(self, cmd, wait=True, files=[], display=False, local=False):
		cmd = self.make_command(cmd, files)
		if not local and not display:
			cmd = cmd[:1] + [self.shell_escape(item) for item in cmd[1:]]
			cmd = " ".join(cmd)
			cmd = "cd " + self.shell_escape(self.path) + "; " + cmd
			ctrl.trace(cmd)
			stdin, stdout, stderr = self.sftp.ssh.exec_command(cmd, timeout=-1)
			while not stdout.channel.exit_status_ready():
				# Only output data if there is data to read in the channel
				if stdout.channel.recv_ready():
					rl, wl, xl = select.select([stdout.channel], [], [], 0.0)
					if len(rl) > 0:
						ctrl.trace(stdout.channel.recv(1024)) # Watch data from stdout

	# - Methods unique to SftpFso -

	def shell_escape(self, arg):
		return "'%s'" % (arg.replace(r"'", r"'\''"), )

class MultiPartForm(object):
	"""Accumulate data for posting form via web service."""

	def __init__(self):
		self.form_fields = []
		self.files = []
		self.boundary = mimetools.choose_boundary()
		return

	def get_content_type(self):
		return 'multipart/form-data; boundary=%s' % self.boundary

	def add_field(self, name, value):
		"""Add a simple field to the form data."""
		self.form_fields.append((name, value))
		return

	def add_file(self, fieldname, filename, fileHandle, mimetype=None):
		"""Add a file to be uploaded."""
		body = fileHandle.read()
		if mimetype is None:
			mimetype = mimetypes.guess_type(filename)[0] or 'application/octet-stream'
		self.files.append((fieldname, filename, mimetype, body))
		return

	def add_file_from_string(self, fieldname, filename, mimetype, body):
		self.files.append((fieldname, filename, mimetype, body))

	def __str__(self):
		"""Return a string representing the form data, including attached files."""

		# Build a list of lists, each containing "lines" of the request.
		# Each part is separated by a boundary string.
		# Once the list is built, return a string where each line is separated by '\r\n'.
		parts = []
		part_boundary = '--' + self.boundary

		# Add the form fields
		parts.extend(
			[ part_boundary,
			  'Content-Disposition: form-data; name="%s"' % name,
			  '',
			  value,
			]
			for name, value in self.form_fields
			)

		# Add the files to upload
		parts.extend(
			[ part_boundary,
			  'Content-Disposition: file; name="%s"; filename="%s"' % \
				 (field_name, filename),
			  'Content-Type: %s' % content_type,
			  '',
			  body,
			]
			for field_name, filename, content_type, body in self.files
			)

		# Flatten the list and add closing boundary marker,
		# then return CR+LF separated data
		flattened = list(itertools.chain(*parts))
		flattened.append('--' + self.boundary + '--')
		flattened.append('')
		return '\r\n'.join(flattened)

class IdataAPI():
	"""Manage communication with iData web service API"""

	def __init__(self, base_url, uid):
		self.base_url = base_url
		self.uid      = uid

	def get_list(    self                       ): return self.call("list"        , {"user_id" :self.uid                                        })
	def delete_dir(  self, collid,path          ): return self.call("delete"      , {"owner_id":self.uid,"id":collid,"path":path                })
	def contents(    self, collid,path          ): return self.call("contents"    , {"owner_id":self.uid,"id":collid,"path":path                })
	def createfolder(self, collid,path,name     ): return self.call("createfolder", {"owner_id":self.uid,"id":collid,"path":path,"name":name    })
	def check(       self, collid,path,name     ): return self.call("check"       , {"owner_id":self.uid,"id":collid,"path":path,"filename":name})
	def delete_file( self, collid,path,name     ): return self.call("delete"      , {"owner_id":self.uid,"id":collid,"path":path,"filename":name})
	def upload(      self, collid,path,name,item): return self.call("upload"      , {"owner_id":self.uid,"id":collid,"path":path,"filename":name},files=item,getresponse=True)
	def finishupload(self, collid,path,name     ): return self.call("finishupload", {"owner_id":self.uid,"id":collid,"path":path,"filename":name},getresponse=True)
	def get(         self, collid,doi,path      ): return self.call("get"         , {"owner_id":self.uid,"id":collid,"doi":doi                  },getresponse=True,download=path)

	def read_in_chunks(self, file_object, chunk_size=1024):
		while True:
			data = file_object.read(chunk_size)
			if not data:
				break
			yield data

	def call(self, method, params={}, files=None, getresponse=False, download=None):
		#ctrl.trace("idata call: "+str(method)+" "+str(params))
		url = self.base_url + method
		vargs = {}
		if files != None:
			vargs["files"] = files
		url = self.base_url + method + "?" + urllib.urlencode(params)
		response = None
		try:
			if download == None and files == None:
				response = urllib2.urlopen(url)
			elif download != None:
				with open(download, "wb") as f2:
					f = urllib2.urlopen(url)
					shutil.copyfileobj(f, f2)
					response = f
			elif files != None:
				fieldname, filepath = files
				mimetype = mimetypes.guess_type(filepath)[0] or 'application/octet-stream'
				with open(filepath, "rb") as f:
					for chunk in self.read_in_chunks(f):
						newurl = url + "&filesize=" + str(len(chunk))
						form = MultiPartForm()
						form.add_file_from_string(fieldname, params["filename"], mimetype, chunk)

						request = urllib2.Request(newurl)
						request.add_header('User-agent', 'PyMOTW (http://www.doughellmann.com/PyMOTW/)')
						body = str(form)
						request.add_header('Content-type', form.get_content_type())
						request.add_header('Content-length', len(body))
						request.add_data(body)

						response = urllib2.urlopen(request)
		except Exception as e:
			ctrl.trace('idata ERROR: "'+str(e)+'"')
			ctrl.trace('url was: "'+url+'"')
			return None

		if getresponse:
			return response
		else:
			return json.load(response)

class IdataFso(RemoteFso):
	def __init__(self, path, collection, content):
		self.collection    = collection
		self.actualcontent = content
		RemoteFso.__init__(self, path)

	def listdir(self):
		contents = model.idata.contents(self.collection["id"],self.path)["contents"]
		files = []
		for content in contents:
			name = content["name"]
			newpath = os.path.join(self.path, name)
			newfso = IdataFso(newpath, self.collection, content)
			files.append(newfso)
			newfso.set_content()
		return files

	def __repr__(self):
		return "IdataFso<" + self.collection["name"] + ":" + self.path + ">"

	def remote_path(self):
		return self.get_doi()

	def isfile(self):
		if self.path == "/":
			return False
		return self.content()["dir-or-file"] != 1

	def isdir(self):
		if self.path == "/":
			return True
		return self.content()["dir-or-file"] == 1

	def mtime(self):
		if "ctime" in self.content():
			ctime = self.content()["ctime"]
			return time.mktime(datetime.datetime.strptime(ctime, "%m/%d/%Y %I:%M %p").timetuple())
		return -1

	def size(self):
		return self.content()["size"]

	def new_for_path(self, path):
		return IdataFso(path, self.collection, None)

	def copy(self, newpath):
		if self.isdir():
			newpath.mkdir()
			for content in self.listdir():
				content.copy(newpath.join(content.basename()))
		else:
			if not newpath.is_remote:
				# Download
				basename   = os.path.basename(self.path)
				parentpath = os.path.dirname(self.path)
				r = model.idata.get(self.collection["id"],self.get_doi(self.path),newpath.path)
			else:
				localself = self.to_temp_local()
				localself.copy(newpath)
				localself.remove()

	def copy_from_local(self, path):
		if path.isdir():
			self.mkdir()
			for content in path.listdir():
				self.join(content.basename()).copyfrom(content)
		else:
			# Upload
			basename   = os.path.basename(self.path)
			parentpath = os.path.dirname(self.path)
			model.idata.upload(self.collection["id"], parentpath, basename, ["file", path.path])
			model.idata.finishupload(self.collection["id"],parentpath, basename)

	def exists(self):
		if self.path == "/": # was: isdir()
			return True
		if self.isdir():
			return True

		basename   = os.path.basename(self.path)
		parentpath = os.path.dirname(self.path)
		content    = model.idata.check(self.collection["id"], parentpath, basename)

		if content == None:
			return False
		else:
			return content["found"] == 1

	def create(self):
		basename = os.path.basename(self.path)
		temppath = model.tempdir.join(basename)
		temppath.create()
		temppath.copy(self)
		temppath.remove()

	def remove(self):
		basename   = os.path.basename(self.path)
		parentpath = os.path.dirname(self.path)
		if self.isfile():
			model.idata.delete_file(self.collection["id"], parentpath, basename)
		elif self.isdir():
			for content in self.listdir():
				content.remove()
			model.idata.delete_dir(self.collection["id"], self.path)

	def mkdir(self):
		basename = os.path.basename(self.path)
		parentpath = os.path.dirname(self.path)
		model.idata.createfolder(self.collection["id"], parentpath, basename)

	def can_edit(self):
		return self.collection["shared"] == "read-write" or self.collection["shared"] == "no"

	def fs(self):
		return ("idata", self.collection["id"], )

	def can_command(self, display=False, local=False):
		return False

	def run_command(self, cmd, wait=True, files=[], display=False, local=False):
		raise NotImplementedError

	# - Methods unique to IdataFso -

	def get_doi(self, path=None):
		if path == None:
			path = self.path
		return "idata::" + path

	def set_content(self):
		content = self.content() # put into cache

	# {u'doi': u'idata:://boulder.tif', u'ctime': u'4/17/2015 5:04 PM', u'name': u'boulder.tif', u'dir-or-file': 2, u'type': u'geospatial', u'size': u'23244'}
	def get_content(self, path):
		#if path != "/":
		for f in self.get_parent().listdir():
			if f == self:
				return f.content()
		return None

	def content(self):
		if self.actualcontent != None:
			return self.actualcontent
		return self.get_content(self.path)

class Model(object):
	def __init__(self):

		# TODO Bring locations in from config file

		# Create filesystem objs (FSOs) for starting locations ("roots" for folder tree)
		startingdirs = []
		self.basenames = {} # Keep list of friendly names for each dir

		# User's home dir
		if not ctrl.options.hidehome:
			startingdirs.append(LocalFso(os.environ['HOME']))
			self.basenames[os.environ['HOME']] = 'Home Folder ('+os.environ['HOME'] + ')'

		# idata
		if options.idataproj != None:
			idatapath = '/srv/irods/' + options.idataproj
			startingdirs.append(LocalFso(idatapath))
			idatadesc = options.idataprojdesc
			if idatadesc == None:
				idatadesc = options.idataproj + " on iData"
			self.basenames[idatapath]  = idatadesc + ' (' + idatapath + ')'

		if not ctrl.options.hideidata:
			startingdirs.append(LocalFso('/srv/irods'))
			self.basenames['/srv/irods']  = 'Projects on iData (/srv/irods)'

		# Tool run's session directory
		if not ctrl.options.hidesession:
			if os.environ.get('SESSIONDIR'):
				startingdirs.append(LocalFso(os.environ['SESSIONDIR']))
				self.basenames[os.environ['SESSIONDIR']] = 'Session Folder ('+os.environ['SESSIONDIR']+')'

		# sdata
		if not ctrl.options.hidesdata:
			startingdirs.append(LocalFso('/home/sdata/'+os.environ['USER']))
			self.basenames['/home/sdata/'+os.environ['USER']]  = 'SDATA Folder (/home/sdata/'+os.environ['USER']+')'

		self.basedirs = [d for d in startingdirs if d.exists()]
#		if self.basedirs == []:
#			ctrl.trace('ERROR: No starting location(s) exist!')
#			exit(-1)

		# Prepare for iData connection
		self.idatauid = os.getuid()
		if self.idatauid == 1000:
			self.idatauid = 2359
			self.trace("WARNING: iData uid OVERRIDE")

		self.idata = IdataAPI('http://mygeohub.org/api/idata/collection/',self.idatauid);

		self.tempdir = LocalFso(tempfile.mkdtemp())

		self.ssh_connections  = []
		self.sftp_connections = []

	def start_ssh(self, hostname, username, password, location):

		if not paramiko_available:
			return

		ssh = paramiko.SSHClient()
		ssh.load_host_keys(os.path.expanduser(os.path.join("~", ".ssh", "known_hosts")))
		ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
		ssh.connect(hostname, username=username, password=password)
		self.ssh_connections.append(ssh)

		stdin, stdout, stderr = ssh.exec_command("id -u " + username)
		uid = int(stdout.read().strip())
		stdin, stdout, stderr = ssh.exec_command("id -g " + username)
		gid = int(stdout.read().strip())

		sftp = ssh.open_sftp()
		self.sftp_connections.append(sftp)

		sftp.uid = uid
		sftp.gid = gid
		sftp.ssh = ssh

		path = SftpFso(sftp, location, username + "@" + hostname)

		if not path.exists():
			location = "/"
			path = SftpFso(sftp, location, username + "@" + hostname)

		return path

	def start_idata(self, collection):
		path = IdataFso("/", collection, None)
		return path

	def cleanup(self):
		self.tempdir.remove()
		for ssh_connection in self.sftp_connections:
			ssh_connection.close()
		for ssh_connection in self.ssh_connections:
			ssh_connection.close()

# ============================
# === View classes - handle UI
# ============================

class FolderTree(wx.TreeCtrl):
	def __init__(self, parent, id=-1, pos=wx.DefaultPosition,
				 size=wx.DefaultSize, style=wx.TR_DEFAULT_STYLE | wx.TR_HIDE_ROOT,
				 validator=wx.DefaultValidator, name='',
				 file_filter=('*.*')):

		wx.TreeCtrl.__init__(self,parent,id,pos,size,style,validator,name)

		self.file_filter = file_filter

		il = wx.ImageList(16,16)
		self.fldridx     = il.Add(wx.ArtProvider.GetBitmap(wx.ART_FOLDER     ,wx.ART_OTHER, (16,16)))
		self.fldropenidx = il.Add(wx.ArtProvider.GetBitmap(wx.ART_FOLDER     ,wx.ART_OTHER, (16,16)))
		self.fileidx     = il.Add(wx.ArtProvider.GetBitmap(wx.ART_NORMAL_FILE,wx.ART_OTHER, (16,16)))
		self.rootidx     = il.Add(wx.ArtProvider.GetBitmap(wx.ART_HARDDISK   ,wx.ART_OTHER, (16,16)))
		self.AssignImageList(il)

		self.allpaths    = []
		self.roots       = []

		self.root = self.AddRoot('Root')
		self.SetItemImage(self.root,self.fldridx    ,wx.TreeItemIcon_Normal)
		self.SetItemImage(self.root,self.fldropenidx,wx.TreeItemIcon_Expanded)
		self.workspacedirs = []

		try:
			self.Expand(self.root)
		except:
			# not if we have a hidden root  # TODO What does this mean?
			pass

	def add_root_folder(self, folder, expand=True, select=True, name=None, index=None):
		if name == None:
			name = folder.path
		item = self.add_item(self.root, folder, name, rootitem=True, index=index)
		if index == None:
			index = len(self.roots)
		self.roots.insert(index, [folder, item])
		if expand:
			self.Expand(item)
		if select:
			self.SelectItem(item)
		self.refresh()

	def get_root(self, path):
		for p, r in self.roots:
			if p == path:
				return r

	def removeworkspacedirs(self):
		for oldworkspacedir in self.workspacedirs:
			if oldworkspacedir != None:
				rootitem = self.get_root(oldworkspacedir)
				self.rem_children(rootitem)
				self.del_item(oldworkspacedir, rootitem)
				self.roots.remove([oldworkspacedir, rootitem])
		self.workspacedirs = []

	def setworkspacedir(self, workspacedir, desc=None):
		if not isinstance(workspacedir, Fso):
			workspacedir = LocalFso(os.path.abspath(os.path.expanduser(workspacedir)))

		if workspacedir.exists():
			if desc == None:
				desc = "Shortcut (" + workspacedir.current()+")"
			else:
				desc += " (" + workspacedir.current() + ")"
			self.add_root_folder(workspacedir, name=desc, index=0, expand=False)
			self.workspacedirs.append(workspacedir)

	def expanding(self, event):
		item = event.GetItem()
		self.add_tree_nodes(item, self.GetPyData(item))

	def rem_children(self, parent):
		todelete = []
		i, cookie = self.GetFirstChild(parent)
		while i.IsOk():
			self.rem_children(i)
			p = self.GetPyData(i)
			todelete.append([p, i])
			i, cookie = self.GetNextChild(parent, cookie)

		for p, i in todelete:
			self.del_item(p, i)

	def collapsing(self, event):
		item          = event.GetItem()
		folder        = self.GetPyData(item)
		itemstodelete = []
		self.rem_children(item)

	def del_item(self, path, item):
		if [path, item] in self.allpaths:
			self.rem_children(item)
			self.allpaths.remove([path, item])
			self.Delete(item)

	def add_item(self, parentItem, itempath, name=None, rootitem=False, index=None):
		if name == None:
			name = itempath.name()

		if index == None:
			newItem = self.AppendItem(parentItem, name)#folder)
		else:
			newItem = self.InsertItemBefore(parentItem, index, name)

		self.SetItemHasChildren(newItem, True)

		if rootitem:
			self.SetItemImage(newItem, self.rootidx    , wx.TreeItemIcon_Normal)
			self.SetItemImage(newItem, self.rootidx    , wx.TreeItemIcon_Expanded)
		else:
			self.SetItemImage(newItem, self.fldridx    , wx.TreeItemIcon_Normal)
			self.SetItemImage(newItem, self.fldropenidx, wx.TreeItemIcon_Expanded)

		self.SetPyData(newItem, itempath)
		self.Bind(wx.EVT_TREE_ITEM_EXPANDING , self.expanding )
		self.Bind(wx.EVT_TREE_ITEM_COLLAPSING, self.collapsing)
		self.allpaths.append([itempath, newItem])

		return newItem

	def get_child_nodes(self, rootfolder):
		items   = rootfolder.listdir()
		items   = sorted(items,key=lambda f : f.basename().lower())
		folders = []

		for item in items:
			if not item.isdir():
				continue
			if item.hidden() and not ctrl.showhidden:
				continue
			folders.append(item)

		return folders

	def add_tree_nodes(self, parentItem, rootfolder):
		folders = self.get_child_nodes(rootfolder)
		for item in folders:
			self.add_item(parentItem, item)

	def get_path(self):
		return self.GetPyData(self.GetSelection())

	def is_parent(self, parent, child):
		cparent = child
		while cparent != self.root:
			if parent == cparent:
				return True
			cparent = self.GetItemParent(cparent)
		return False

	def get_item(self, path, root=None):
		for p, item in self.allpaths:
			if path == p and (root == None or self.is_parent(root, item)):
				return item
		if view != None:
			view.status(ERR_NOT_FOUND)

	def get_parents(self, path):
		parents = path.get_parents()
		parents = list(reversed(parents)) + [path]

		maxindex = -1
		pathroot = None
		for root, rootitem in self.roots:
			if root in parents:
				index = parents.index(root)
				if index > maxindex:
					pathroot = root
					maxindex = index

		if pathroot == None:
			return

		rootindex = parents.index(pathroot)
		parents   = parents[rootindex:]

		return parents

	def expand_path(self, path, include=True):
		parents = self.get_parents(path)
		if parents != None:
			pathroot = parents[0]
			toexpand = parents
			if include == False:
				toexpand = toexpand[:-1]
			for sub in toexpand:
				self.Expand(self.get_item(sub, self.get_root(pathroot)))

	def set_path(self, path):
		self.expand_path(path, include=False)
		target = self.get_item(path)
		if target:
			self.SelectItem(target)

	def refresh(self):
		selectedpath = self.get_path()
		if selectedpath == None:
			if len(self.roots) == 0:
				return
			selectedpath = self.roots[0][0]

		toremove = []
		expanded = []

		for path, item in self.allpaths:

			if not path.exists():
#				item = self.rempath(path)
				toremove.append([path, item])
			else:
				if self.IsExpanded(item):
					expanded.append(path)
					children = []

					for newpath, newitem in self.allpaths:
						if newpath.ischild(path):
							children.append(newpath)

					newchildren = self.get_child_nodes(path)

					for newchild in newchildren:
						if not newchild in children:
							self.add_item(item, newchild)

		for item in toremove:
			self.del_item(*item)

		for root, rootitem in self.roots:
			if self.IsExpanded(rootitem):
				self.Collapse( rootitem)
				self.Expand(   rootitem)

		for path in expanded:
			self.expand_path(path)

		self.set_path(selectedpath)

class ContentsListCtrl(wx.ListCtrl, ListCtrlAutoWidthMixin):
	def __init__(self, parent, ID, pos=wx.DefaultPosition,
			 size=wx.DefaultSize, style=0):
		wx.ListCtrl.__init__(self, parent, ID, pos, size, style)
		ListCtrlAutoWidthMixin.__init__(self)
		self.setResizeColumn(0)

class View(wx.Frame):

	FileOps   = enum("Edit", "NewFolder", "NewFile", "Cut", "Copy", "Paste", "Upload", "Download", "Rename", "Extract", "Compress", "Delete")
	OpWidgets = enum("Browser", "FolderTree", "Menu", "Finish")
	Sorts     = enum("Name", "Size", "Date")

	def __init__(self):

		self.app = wx.App(False)

		self.sort = View.Sorts.Name

		title = TTL_NORMAL
		if   ctrl.options.mode == Controller.Mode.Open: title = TTL_OPEN
		elif ctrl.options.mode == Controller.Mode.Save: title = TTL_SAVE

		wx.Frame.__init__(self, None, -1, title, wx.DefaultPosition, wx.Size(800, 500))

		self.lochistory = [] # Support "go back" features

		# Build menus

        # Simultaneously build File, Edit, and list of items for context (right-click) menus
		self.file_opmenuitems = {}

		def file_opmenuitem(menu, id, text, file_op):
			item = self.menu_append_bind(menu, id, text, functools.partial(self.file_op, operation=file_op, operation_from=View.OpWidgets.Menu))
			self.file_opmenuitems[item] = file_op
			return item

		fileMenu = wx.Menu()
		item = file_opmenuitem(fileMenu ,wx.ID_NEW       ,ITM_FILE    ,View.FileOps.NewFile  )
		item = file_opmenuitem(fileMenu ,wx.ID_OPEN      ,ITM_FOLDER  ,View.FileOps.NewFolder)
		item = file_opmenuitem(fileMenu ,wx.ID_EDIT      ,ITM_EDIT    ,View.FileOps.Edit     )
		item = file_opmenuitem(fileMenu ,wx.ID_PROPERTIES,ITM_RENAME  ,View.FileOps.Rename   )
		fileMenu.AppendSeparator()
		item = file_opmenuitem(fileMenu ,-1              ,ITM_COMPRESS,View.FileOps.Compress )
		item = file_opmenuitem(fileMenu ,-1              ,ITM_EXTRACT ,View.FileOps.Extract  )
		fileMenu.AppendSeparator()
		item = self.menu_append_bind(               fileMenu    ,wx.ID_EXIT      ,ITM_EXIT    ,self.exit                                                     )

		editMenu = wx.Menu()
		item = file_opmenuitem(editMenu ,wx.ID_CUT       ,ITM_CUT     ,View.FileOps.Cut      )
		item = file_opmenuitem(editMenu ,wx.ID_COPY      ,ITM_COPY    ,View.FileOps.Copy     )
		item = file_opmenuitem(editMenu ,wx.ID_PASTE     ,ITM_PAST    ,View.FileOps.Paste    )
		editMenu.AppendSeparator()
		item = file_opmenuitem(editMenu ,wx.ID_DELETE    ,ITM_DELETE  ,View.FileOps.Delete   )

        # Build other menus
		viewMenu = wx.Menu()
		item = self.menu_append_bind(               viewMenu    ,wx.ID_BACKWARD ,ITM_BACK     ,self.go_back                                                  )
		item = self.menu_append_bind(               viewMenu    ,wx.ID_UP       ,ITM_UP       ,self.go_up                                                    )
		item = self.menu_append_bind(               viewMenu    ,wx.ID_REFRESH  ,ITM_REFRESH  ,functools.partial(self.refresh_files, force=True)             )
		viewMenu.AppendSeparator()
		self.hiddenitem = self.menu_append_bind(    viewMenu    ,-1             ,ITM_HIDDEN   ,self.toggle_hidden                              ,wx.ITEM_CHECK)
		viewMenu.AppendSeparator()
		self.snameitem  = self.menu_append_bind(    viewMenu    ,-1             ,ITM_SORTNAME ,functools.partial(self.do_sort, View.Sorts.Name),wx.ITEM_RADIO)
		self.ssizeitem  = self.menu_append_bind(    viewMenu    ,-1             ,ITM_SORTSIZE ,functools.partial(self.do_sort, View.Sorts.Size),wx.ITEM_RADIO)
		self.sdateitem  = self.menu_append_bind(    viewMenu    ,-1             ,ITM_SORTDATE ,functools.partial(self.do_sort, View.Sorts.Date),wx.ITEM_RADIO)

		transferMenu = wx.Menu()
		item = file_opmenuitem(transferMenu                     ,-1             ,ITM_ULOAD    ,View.FileOps.Upload                                           )
		item = file_opmenuitem(transferMenu                     ,-1             ,ITM_DLOAD    ,View.FileOps.Download                                         )

		connectMenu = wx.Menu()
		self.sshmenuitem   = self.menu_append_bind( connectMenu ,-1             ,ITM_SFTP     ,self.ssh_dialog                                               )
		self.idatamenuitem = self.menu_append_bind( connectMenu ,-1             ,ITM_IDATA    ,self.idata_dialog                                             )
		self.sshmenuitem.Enable(paramiko_available)
		self.idatamenuitem.Enable(os.uname()[1] == 'MyGeoHUB')

		menubar = wx.MenuBar()
		menubar.Append(fileMenu    , MNU_FILE)
		menubar.Append(editMenu    , MNU_EDIT)
		menubar.Append(viewMenu    , MNU_VIEW)
		menubar.Append(transferMenu, MNU_TRANSFER)
		menubar.Append(connectMenu , MNU_CONNECT)
		self.SetMenuBar(menubar)
		fileMenu.Bind(    wx.EVT_MENU_OPEN, self.on_menu_open)
		editMenu.Bind(    wx.EVT_MENU_OPEN, self.on_menu_open)
		viewMenu.Bind(    wx.EVT_MENU_OPEN, self.on_menu_open)
		transferMenu.Bind(wx.EVT_MENU_OPEN, self.on_menu_open)
		connectMenu.Bind( wx.EVT_MENU_OPEN, self.on_menu_open)

		# Build toolbar
		tb = self.CreateToolBar( wx.TB_HORIZONTAL | wx.NO_BORDER | wx.TB_FLAT | wx.TB_HORZ_TEXT)
		backtoolbaritem     = tb.AddLabelTool(-1,LBL_BACK    ,bitmap=wx.ArtProvider.GetBitmap(wx.ART_GO_BACK))
		uptoolbaritem       = tb.AddLabelTool(-1,LBL_UP      ,bitmap=wx.ArtProvider.GetBitmap(wx.ART_GO_UP  ))
		refreshtoolbaritem  = tb.AddLabelTool(-1,LBL_REFRESH ,bitmap=wx.ArtProvider.GetBitmap('gtk-refresh' ))
		tb.AddSeparator()
		uploadtoolbaritem   = tb.AddLabelTool(-1,"Upload",    bitmap=wx.ArtProvider.GetBitmap('gtk-goto-top'))
		downloadtoolbaritem = tb.AddLabelTool(-1,"Download",  bitmap=wx.ArtProvider.GetBitmap('gtk-goto-bottom'))
		tb.Realize()
		self.Bind(wx.EVT_TOOL,self.go_back      ,backtoolbaritem)
		self.Bind(wx.EVT_TOOL,self.go_up        ,uptoolbaritem)
		self.Bind(wx.EVT_TOOL,functools.partial(self.refresh_files, force=True),refreshtoolbaritem)
		self.Bind(wx.EVT_TOOL,functools.partial(self.file_op, operation=View.FileOps.Upload, operation_from=View.OpWidgets.Menu)         ,uploadtoolbaritem)
		self.Bind(wx.EVT_TOOL,functools.partial(self.file_op, operation=View.FileOps.Download, operation_from=View.OpWidgets.Menu)         ,downloadtoolbaritem)

		# Set up window

		# Main panel
		main = wx.Panel(self)
		mainsizer = wx.BoxSizer(wx.VERTICAL)
		main.SetSizer(mainsizer)

		self.filesplitter = wx.SplitterWindow(main, -1, style=wx.SP_3D)
		mainsizer.Add(self.filesplitter, 1, wx.EXPAND)

		# Left side: Navigation
		dirpanel = wx.Panel(self.filesplitter)
		dirsizer = wx.BoxSizer(wx.VERTICAL)
		dirpanel.SetSizer(dirsizer)

		self.foldertree = FolderTree(dirpanel)
		dirsizer.Add(self.foldertree, 1, wx.EXPAND)

		# Right side: Folder Contents (files, other folders)
		contentspanel = wx.Panel(self.filesplitter)
		self.contentssizer = wx.BoxSizer(wx.VERTICAL)
		contentspanel.SetSizer(self.contentssizer)

		self.textpanel = wx.Panel(contentspanel)
		self.contentssizer.Add(self.textpanel, 0, wx.EXPAND)
		textsizer = wx.BoxSizer(wx.VERTICAL)
		self.textpanel.SetSizer(textsizer)
		self.texttext = wx.StaticText(self.textpanel, -1, PMT_READY)
		textsizer.Add(self.texttext, 0, wx.TOP | wx.BOTTOM | wx.LEFT | wx.RIGHT | wx.EXPAND, border=10)

		self.contents = ContentsListCtrl(contentspanel,-1,style=wx.LC_REPORT)

		self.contentssizer.Add(self.contents, 1, wx.EXPAND)
		self.contents.Bind(wx.EVT_LIST_ITEM_ACTIVATED  , self.click_item)
		self.contents.Bind(wx.EVT_LIST_ITEM_SELECTED   , self.on_selection)
		self.contents.Bind(wx.EVT_LIST_ITEM_DESELECTED , self.on_selection)
		self.contents.Bind(wx.EVT_LIST_ITEM_RIGHT_CLICK, self.right_click)
		self.contents.Bind(wx.EVT_LIST_COL_CLICK       , self.click_column)

		# Add controls for "open" and "save" modes

		# Open: just a button
		self.openpanel = wx.Panel(main) # contentspanel
		mainsizer.Add(self.openpanel, 0, wx.TOP | wx.BOTTOM | wx.RIGHT | wx.EXPAND, border=10) # self.contentssizer
		opensizer = wx.BoxSizer(wx.HORIZONTAL)
		self.openpanel.SetSizer(opensizer)

		self.filterdropdown = wx.Choice(self.openpanel)
		self.filterdropdown.Bind(wx.EVT_CHOICE, self.onfilterchanged)

		opensizer.AddStretchSpacer()

		self.opencancelbutton = wx.Button(self.openpanel, -1, LBL_CANCEL)
		self.opencancelbutton.Bind(wx.EVT_BUTTON, self.exit)
		opensizer.Add(self.opencancelbutton, 0, wx.LEFT, border=10)

		self.openbutton = wx.Button(self.openpanel, -1, LBL_OPEN)
		self.openbutton.Bind(wx.EVT_BUTTON, self.open_files)
		opensizer.Add(self.openbutton, 0, wx.LEFT, border=10)

		# Save: a text entry and a button
		self.savepanel = wx.Panel(main) # contentspanel
		mainsizer.Add(self.savepanel, 0, wx.TOP | wx.BOTTOM | wx.LEFT | wx.RIGHT | wx.EXPAND, border=10) # self.contentssizer
		savesizer = wx.BoxSizer(wx.HORIZONTAL)
		self.savepanel.SetSizer(savesizer)

		self.savetext = wx.TextCtrl(self.savepanel, -1)
		savesizer.Add(self.savetext, 1)

		self.savecancelbutton = wx.Button(self.savepanel, -1, LBL_CANCEL)
		self.savecancelbutton.Bind(wx.EVT_BUTTON, self.exit)
		savesizer.Add(self.savecancelbutton, 0, wx.LEFT, border=10)

		self.savebutton = wx.Button(self.savepanel, -1, LBL_SAVE)
		self.savebutton.Bind(wx.EVT_BUTTON, self.open_files)
		savesizer.Add(self.savebutton, 0, wx.LEFT, border=10)

		# Add base directory(s)
		first = True
		for basedir in model.basedirs:
			self.foldertree.add_root_folder(basedir,expand=False,select=first,name=model.basenames[basedir.path])
			if first:
				first = False

		imgsize = 16
		imglist = wx.ImageList(imgsize,imgsize)
		imglist.Add(wx.ArtProvider.GetBitmap(wx.ART_FOLDER, wx.ART_OTHER, (imgsize,imgsize)))
		imglist.Add(wx.ArtProvider.GetBitmap(wx.ART_NORMAL_FILE, wx.ART_OTHER, (imgsize,imgsize)))
		self.contents.AssignImageList(imglist, wx.IMAGE_LIST_SMALL)

		self.filesplitter.SplitVertically(dirpanel, contentspanel, sashPosition=200)
		# self.filesplitter.Unsplit(dirpanel)
		self.Layout()
		wx.EVT_TREE_SEL_CHANGED(self, self.foldertree.GetId(), self.tree_selection_changed)
		self.foldertree.Bind(wx.EVT_TREE_ITEM_RIGHT_CLICK, self.right_click)

		# Status bar
		self.sb = self.CreateStatusBar()
		self.status(STS_STARTUP)

		self.refresh_files()
		self.Centre()

		self.Bind(wx.EVT_CLOSE, self.exit)

	def restore(self):
		self.Show()
		self.sb.Show()
		self.openpanel.Hide()
		self.savepanel.Hide()
		self.textpanel.Hide()

		self.filterdropdown.Clear()

		ctrl.options.filterdict = []
		if ctrl.options.mode == Controller.Mode.Open:
			self.openpanel.Show()
			self.sb.Hide()

			if ctrl.options.filter != None:
				filter = ctrl.options.filter
				filter = [("." + extension) if not ("." in extension) else extension for extension in filter] # make sure extensions begin with .
				filtertext = " ".join(filter)
				self.filterdropdown.Append(filtertext)
				ctrl.options.filterdict.append(filter)

		self.filterdropdown.Append("All Files")
		self.filterdropdown.Select(0)
		ctrl.options.filterdict.append(None)

		if ctrl.options.mode == Controller.Mode.Save:
			self.savepanel.Show()
			self.sb.Hide()

		if ctrl.options.mode == Controller.Mode.Open or ctrl.options.mode == Controller.Mode.Save:
			self.SetWindowStyle(wx.STAY_ON_TOP)
		else:
			self.SetWindowStyle(0)

		if ctrl.options.text:
			self.texttext.SetLabel(options.text)
			self.textpanel.Show()

		self.openpanel.GetParent().GetSizer().Layout()
		self.foldertree.removeworkspacedirs()
		if ctrl.options.workspacedir != None:
			if ctrl.options.workspacedesc == None:
				ctrl.options.workspacedesc = []
			if isinstance(ctrl.options.workspacedir, list):
				workspacedesc = ctrl.options.workspacedesc + [None] * (len(ctrl.options.workspacedir) - len(ctrl.options.workspacedesc))
				for index, workspacedir in enumerate(reversed(ctrl.options.workspacedir)):
					desc = list(reversed(workspacedesc))[index]
					self.foldertree.setworkspacedir(workspacedir, desc)
			else:
				self.foldertree.setworkspacedir(ctrl.options.workspacedir)

		if len(self.foldertree.roots) == 0:
			print "ERROR: No starting location(s) exist!"
			sys.exit(-1)

		self.unselect()
		if ctrl.options.mode == Controller.Mode.Save:
			self.savetext.SetValue(ctrl.options.suggest)
		self.refresh_files()

		title = TTL_NORMAL
		if   ctrl.options.mode == Controller.Mode.Open: title = TTL_OPEN
		elif ctrl.options.mode == Controller.Mode.Save: title = TTL_SAVE
		self.SetTitle(title)

	def getfilter(self):
		select = self.filterdropdown.GetSelection()
		filter = ctrl.options.filterdict[select]
		return filter

	def thread_request(self, method):
		'''Safe way to request action from another thread'''
		wx.CallAfter(method)

	def on_menu_open(self, event):
		for item in self.file_opmenuitems:
			file_op = self.file_opmenuitems[item]
			item.Enable(self.verify_operation(file_op, View.OpWidgets.Menu))

	def menu_append_bind(self,menu,wxid,text,callback,thekind=None):
		if thekind:
			newitem = menu.Append(wxid,text,kind=thekind)
		else:
			newitem = menu.Append(wxid,text)
		self.Bind(wx.EVT_MENU, callback, newitem)
		return newitem

	def status(self,text):
		self.sb.SetStatusText(text)

	def do_sort(self, sort, event=None):
		self.sort = sort
		check = None
		if   self.sort == View.Sorts.Name: check = self.snameitem
		elif self.sort == View.Sorts.Size: check = self.ssizeitem
		elif self.sort == View.Sorts.Date: check = self.sdateitem
		check.Check(True)
		self.refresh_files()

	def click_column(self, event):
		if   event.Column == 0: self.do_sort(View.Sorts.Name)
		elif event.Column == 1: self.do_sort(View.Sorts.Size)
		elif event.Column == 2: self.do_sort(View.Sorts.Date)

	def exit(self, event=None):
		ctrl.exit()

	def toggle_hidden(self, event=None):
		ctrl.toggle_hidden()
		self.hiddenitem.Check(ctrl.showhidden)
		self.refresh_files()

	def open_files(self, event=None):
		selection_all, _, use_dir, _, _, _, _, _, _ = self.get_operation_info(View.OpWidgets.Finish)
		ctrl.finish(selection_all, use_dir, self.get_save_name())

	def get_save_name(self):
		if ctrl.options.mode == Controller.Mode.Save:
			text = self.savetext.GetValue()
			basenames = text.split(",")
			basenames = [basename.strip() for basename in basenames]
			return basenames
		else:
			return [""]

	def on_selection(self, event):
		selection_all, _, _, _, _, _, _, _, _ = self.get_operation_info(View.OpWidgets.Browser)
		if selection_all != None and selection_all != []:
			if not any([item.isdir() for item in selection_all]):
				basenames = ", ".join([item.basename() for item in selection_all])
				self.savetext.SetValue(basenames)

	def tree_selection_changed(self, event=None):
		self.lochistory.append(self.foldertree.get_path())
		self.refresh_file_list()

	def upload_file(self, event=None, where=None):
		if where == None:
			where = self.get_path()
		ctrl.upload_file(where)
		# self.refresh_files(event=None, force=True)

	def download_file(self, event=None, items=None):
		if items == None:
			items = self.get_selected_paths()
		ctrl.download_file(items, self.foldertree.get_path())

	def refresh_files(self, event=None, force=False):
		if force:
			self.get_path().set_cache_time_obj()
		self.foldertree.refresh()
		self.refresh_file_list()

	def refresh_file_list(self):
		if view == None:
			return

		path = self.foldertree.get_path()

		# Get state
		selecteditems = self.get_selected_paths()
		focus = self.contents.GetFocusedItem()
		xpos, ypos = self.contents.GetScrollPos(wx.VERTICAL), self.contents.GetScrollPos(wx.HORIZONTAL)

		self.status('Ok')
		if path:
			self.status(path.current())

		if path != None:
			list = path.listdir()

			self.contents.ClearAll()
			self.contents.InsertColumn(0, LBL_NAME); self.contents.SetColumnWidth(0, 200)
			self.contents.InsertColumn(1, LBL_SIZE); self.contents.SetColumnWidth(1, 100)
			self.contents.InsertColumn(2, LBL_DATE); self.contents.SetColumnWidth(2, 170)

			self.contentssizer.Layout()

			files   = []
			folders = []

			# Get listing
			for path in list:

				if path.hidden() and not ctrl.showhidden:
					continue

				if path.isdir():
					folders.append(path)

				if path.isfile():
					files.append(path)

			folders.sort(reverse=True, key=lambda x : x.name().lower())
			files.sort(  reverse=True, key=lambda x : x.name().lower())

			filter = view.getfilter()
			if filter != None:
				files = [f for f in files if any([f.basename().endswith(extension) for extension in filter])] # filter out files without these extensions

			if   self.sort == View.Sorts.Name: pass
			elif self.sort == View.Sorts.Size: files.sort(reverse = True, key = lambda x : x.size())
			elif self.sort == View.Sorts.Date: files.sort(reverse = True, key = lambda x : x.mtime())

			items = files+folders

			# Display listing
			for item in items:
				sizestr = ''
				timestr = ''
				pos     = self.contents.InsertStringItem(0, item.name())

				if item.isfile():
					sizestr = str(item.size()) + LBL_BYTES
					mtime = item.mtime()
					if mtime != -1:
						timestr = time.ctime(mtime)
					self.contents.SetItemImage(pos, 1)
				else:
					self.contents.SetItemImage(pos, 0)

				self.contents.SetStringItem(pos, 1, sizestr)
				self.contents.SetStringItem(pos, 2, timestr)

				# Re-select items
				if item in selecteditems:
					self.contents.Select(pos)

		# Restore state
		try:
			self.contents.Focus(focus)
		except:
			# couldn't restore focus
			pass
		try:
			self.contents.SetScrollPos(wx.VERTICAL  , xpos)
			self.contents.SetScrollPos(wx.HORIZONTAL, ypos)
		except:
			# couldn't restore scroll pos
			pass

	def onfilterchanged(self, event=None):
		self.refresh_files()

	def get_item_path(self, item):
		return self.get_path(item.GetText())

	def get_path(self, name=None):
		if name == None:
			return self.foldertree.get_path()
		else:
			return self.foldertree.get_path().join(name)

	def click_item(self, event=None, item=None):
		if item == None:
			item = event.GetItem()
		path = self.get_item_path(item)
		if path.isdir():
			self.foldertree.set_path(path)

	def open_items(self, event=None, items=None):
		if items == None:
			items = [self.get_item_path(event.GetItem())]
		for item in items:
			ctrl.open_item(item,self.get_path())

	def new_folder(self, event=None, where=None):
		if where == None:
			where = self.get_path()
		test = wx.TextEntryDialog(None, 'New folder name:', 'New Folder')
		if test.ShowModal() == wx.ID_OK:
			foldername = test.GetValue()
			ctrl.new_folder(foldername, where)
			self.refresh_files()

	def new_file(self, event=None, where=None):
		if where == None:
			where = self.get_path()
		test = wx.TextEntryDialog(None, PMT_NEW_FILE, TTL_NEW_FILE)
		if test.ShowModal() == wx.ID_OK:
			filename = test.GetValue()
			ctrl.new_file(filename, where)
			self.refresh_files()

	def delete_items(self, event=None, items=None):
		ret = wx.MessageBox(PMT_DELETE, TTL_DELETE, wx.YES_NO | wx.CENTRE | wx.NO_DEFAULT, self)
		if ret == wx.YES:
			ctrl.delete_items(items)
			self.refresh_files()

	def rename_file(self, event=None, item=None):
		path = item
		name = path.basename()
		test = wx.TextEntryDialog(None, PMT_NEW_FILE, TTL_NEW_FILE, name)
		if test.ShowModal() == wx.ID_OK:
			newname = test.GetValue()
			newpath = self.get_path(newname)
			ctrl.rename_file(path,newpath)
			self.refresh_files()

	def compress(self, event=None, items=None):
		test = wx.TextEntryDialog(None, PMT_ARCHIVE_NAME, TTL_COMPRESS)
		if test.ShowModal() == wx.ID_OK:
			newname = test.GetValue()
			ctrl.compress(items, self.get_path(), newname)
			self.refresh_files()

	def show_error(self, message):
		ret = wx.MessageBox(message, 'Error', wx.OK | wx.CENTRE | wx.ICON_ERROR, self)
		return ret

	def ask(self, message):
		ret = wx.MessageBox(message, 'Question', wx.YES_NO | wx.CENTRE | wx.NO_DEFAULT, self)
		return (ret == wx.YES)

	def extract(self, event=None, item=None, in_dir=None):
		ctrl.extract(item=item, in_dir=in_dir)
		self.refresh_files()

	def get_operation_info(self, operation_from):
		'''Retrun information on the current operation and its associated selected item(s) and directory'''

		# Selection(s) made?
		if operation_from == View.OpWidgets.FolderTree:
			selection_all  = [self.foldertree.get_path()]
			selection_made = True
		else:
			selection_all  = self.get_selected_paths()
			selection_made = selection_all != []

		# Folder operation ok?
		if   operation_from == View.OpWidgets.Browser:    folder_op_ok = not selection_made
		elif operation_from == View.OpWidgets.FolderTree: folder_op_ok = True
		elif operation_from == View.OpWidgets.Menu:       folder_op_ok = True
		elif operation_from == View.OpWidgets.Finish:     folder_op_ok = False
		else: ctrl.trace('ERROR: Unknown OpWidgets value!')

		# Selected only one item?
		selection_only_one = None
		selected_single    = False
		if len(selection_all) == 1:
			selection_only_one = selection_all[0]
			selected_single    = True

		# Which directory?
		in_dir = self.get_path()
		if selected_single and selection_only_one.isdir() and (not operation_from == View.OpWidgets.Menu):
			use_dir = selection_only_one
		else:
			use_dir = in_dir

		# Allowed to edit or download?
		can_edit     = all(item.can_edit() for item in selection_all) and in_dir.can_edit()
		can_download = all(item.can_download() for item in selection_all)

		# Tree operation ok?
		prevent_tree_op = (not folder_op_ok or operation_from == View.OpWidgets.Menu)

		return selection_all, selection_only_one, use_dir, selection_made, selected_single, folder_op_ok, prevent_tree_op, can_edit, can_download

	def verify_operation(self, operation, operation_from):
		'''Enforce UI logic'''

		selection_all, selection_only_one, use_dir, selection_made, selected_single, folder_op_ok, prevent_tree_op, can_edit, can_download = self.get_operation_info(operation_from)

		if   operation == View.FileOps.Edit:      return selection_made and selected_single and (not selection_only_one.isdir()) and can_edit and all(item.can_command(display=True) for item in selection_all)
		elif operation == View.FileOps.NewFolder: return folder_op_ok and can_edit
		elif operation == View.FileOps.NewFile:   return folder_op_ok and can_edit
		elif operation == View.FileOps.Cut:       return selection_made and can_edit and (prevent_tree_op)
		elif operation == View.FileOps.Copy:      return selection_made
		elif operation == View.FileOps.Paste:     return can_edit and (ctrl.clipboard != [])
		elif operation == View.FileOps.Upload:    return folder_op_ok and can_edit and use_dir.can_command(local=True)
		elif operation == View.FileOps.Download:  return selection_made and can_download
		elif operation == View.FileOps.Rename:    return selection_made and can_edit and selected_single and (prevent_tree_op)
		elif operation == View.FileOps.Extract:   return selection_made and can_edit and selected_single and selection_only_one.can_extract() and all(item.can_command() for item in selection_all)
		elif operation == View.FileOps.Compress:  return selection_made and can_edit and not (selected_single and selection_only_one.can_extract()) and (prevent_tree_op) and all(item.can_command() for item in selection_all)
		elif operation == View.FileOps.Delete:    return selection_made and can_edit and (prevent_tree_op)

	def file_op(self, event=None, operation=None, operation_from=None):

		selection_all, selection_only_one, use_dir, _, _, _, _, _, _ = self.get_operation_info(operation_from)

		if   operation == View.FileOps.Edit:      self.open_items(   items=[selection_only_one])
		elif operation == View.FileOps.NewFolder: self.new_folder(   where=use_dir)
		elif operation == View.FileOps.NewFile:   self.new_file(     where=use_dir)
		elif operation == View.FileOps.Cut:       ctrl.cut_items(    items=selection_all)
		elif operation == View.FileOps.Copy:      ctrl.copy_items(   items=selection_all)
		elif operation == View.FileOps.Paste:     ctrl.paste_items(  where=use_dir)
		elif operation == View.FileOps.Upload:    self.upload_file(  where=use_dir)
		elif operation == View.FileOps.Download:  self.download_file(items=selection_all)
		elif operation == View.FileOps.Rename:    self.rename_file(  item =selection_only_one)
		elif operation == View.FileOps.Extract:   self.extract(      item =selection_only_one, in_dir=use_dir)
		elif operation == View.FileOps.Compress:  self.compress(     items=selection_all)
		elif operation == View.FileOps.Delete:    self.delete_items( items=selection_all)

	def right_click(self, event):
		eventsource = event.GetEventObject()
		folder_op_ok = False
		if eventsource == self.contents:

			selection_made = event.GetIndex() != -1

			if not selection_made:
				# deselect items
				selection = []
				index     = self.contents.GetFirstSelected()

				if index != -1:
					selection.append(index)

				while True:
					index = self.contents.GetNextSelected(index)
					if index == -1:
						break
					selection.append(index)

				for item in selection:
					self.contents.Select(item, False)

			operation_from = View.OpWidgets.Browser
		elif eventsource == self.foldertree:
			operation_from = View.OpWidgets.FolderTree

		# View.FileOps is a constant, call self.file_op with this constant
		items = []
		items += [(ITM_EDIT     ,View.FileOps.Edit,      wx.ID_EDIT  ) ]
		items += [None]
		items += [(ITM_FOLDER   ,View.FileOps.NewFolder, wx.ID_OPEN  ) ]
		items += [(ITM_FILE     ,View.FileOps.NewFile,   wx.ID_NEW   ) ]
		items += [None]
		items += [(ITM_CUT      ,View.FileOps.Cut,       wx.ID_CUT   ) ]
		items += [(ITM_COPY     ,View.FileOps.Copy,      wx.ID_COPY  ) ]
		items += [(ITM_PAST     ,View.FileOps.Paste,     wx.ID_PASTE ) ]
		items += [None]
		items += [(ITM_ULOAD    ,View.FileOps.Upload,    -1          ) ]
		items += [(ITM_DLOAD    ,View.FileOps.Download,  -1          ) ]
		items += [None]
		items += [(ITM_RENAME   ,View.FileOps.Rename,    -1          ) ]
		items += [None]
		items += [(ITM_EXTRACT  ,View.FileOps.Extract,   -1          ) ]
		items += [(ITM_COMPRESS ,View.FileOps.Compress,  -1          ) ]
		items += [None]
		items += [(ITM_DELETE   ,View.FileOps.Delete,    wx.ID_DELETE) ]

		items = [item for item in items if item == None or self.verify_operation(item[1], operation_from)]

		items = [item[0] for item in itertools.groupby(items)] # remove duplicate separators
		if items[0] == None:
			items = items[1:]
		if items == []:
			return
		if items[-1] == None:
			items = items[:-1]

		if items != [] and items[-1] == None:
			items = items[:-1]

		if items != []:
			menu = wx.Menu()
			for item in items:
				if item == None:
					menu.AppendSeparator()
				else:
					label, funct, id = item
					menuitem = menu.Append(id, label)
					funct = functools.partial(self.file_op, operation=funct, operation_from=operation_from)
					self.Bind(wx.EVT_MENU, funct, menuitem)

			eventsource.PopupMenu(menu, event.GetPoint())
			menu.Destroy()

	def go_up(self, event=None):
		parents = self.foldertree.get_path().get_parents()
		if parents:
			self.foldertree.set_path(parents[0])

	def go_back(self, event=None):
		if len(self.lochistory) > 1:
			self.lochistory.pop()
			self.foldertree.set_path(self.lochistory.pop())

	def get_selected_items(self, listCtrl):
		selection = []
		index     = listCtrl.GetFirstSelected()

		if index != -1:
			selection.append(index)

		while True:
			index = listCtrl.GetNextSelected(index)
			if index == -1:
				break
			selection.append(index)

		selection = [listCtrl.GetItem(index) for index in selection]

		return selection

	def get_selected_paths(self):
		selection_all = self.get_selected_items(self.contents)
		paths = [self.get_item_path(item) for item in selection_all]
		return paths

	def unselect(self):
		for item in self.get_selected_items(self.contents):
			self.contents.Select(item.GetId(), on=False)

	def run(self):
		self.Show(True)
		self.app.MainLoop()

	def ssh_dialog(self, event):
		class SshDialog(wx.Frame):
			def __init__(self, parent, sshmethod):
				wx.Frame.__init__(self, parent, -1, TTL_SFTP)
				self.SetWindowStyle(parent.GetWindowStyle())
				self.panel = wx.Panel(self, -1)
				self.sshmethod = sshmethod

				vbox = wx.BoxSizer(wx.VERTICAL)

				self.locentry      = wx.TextCtrl(self.panel, size=(300, -1)                      )
				self.usernameentry = wx.TextCtrl(self.panel, size=(300, -1)                      )
				self.passwordentry = wx.TextCtrl(self.panel, size=(300, -1), style=wx.TE_PASSWORD)

				self.locentry.Bind(     wx.EVT_KEY_DOWN, self.other_entry)
				self.usernameentry.Bind(wx.EVT_KEY_DOWN, self.other_entry)
				self.passwordentry.Bind(wx.EVT_KEY_DOWN, self.pw_entry   )

				grid = wx.FlexGridSizer(rows=5, cols=2, vgap=5, hgap=5)
				grid.AddMany([
					(wx.StaticText(self.panel, label=LBL_HOST), 0, wx.FIXED_MINSIZE), (self.locentry     , 0, wx.EXPAND),
					(wx.StaticText(self.panel, label=LBL_USER), 0, wx.FIXED_MINSIZE), (self.usernameentry, 0, wx.EXPAND),
					(wx.StaticText(self.panel, label=LBL_PASS), 0, wx.FIXED_MINSIZE), (self.passwordentry, 0, wx.EXPAND)
				])

				connectbutton = wx.Button(self.panel, label=LBL_CONNECT)
				connectbutton.Bind(wx.EVT_BUTTON, self.connect   )

				vbox.Add(grid         , flag=wx.TOP | wx.LEFT | wx.RIGHT            , border=15)
				vbox.Add(connectbutton, flag=wx.TOP | wx.LEFT | wx.RIGHT | wx.BOTTOM, border=15)

				self.panel.SetSizerAndFit(vbox)
				self.Fit()
				self.Centre()
				self.Show()

			def other_entry(self, event):
				if event.GetKeyCode() == wx.WXK_RETURN:
					win = event.GetEventObject()
					win.Navigate()
				else:
					event.Skip()

			def pw_entry(self, event):
				if event.GetKeyCode() == wx.WXK_RETURN:
					self.connect()
				else:
					event.Skip()

			def connect(self, event=None):
				hostname = self.hostnameentry.GetValue()
				location = self.locentry.GetValue()
				if location == "":
					location = "/"
				username = self.usernameentry.GetValue()
				password = self.passwordentry.GetValue()
				self.sshmethod(hostname, username, password, location)
				self.Destroy()

		SshDialog(self, ctrl.start_ssh)

	def idata_dialog(self, event=None):
		class IdataDialog(wx.Frame):
			def __init__(self, parent, method, choices):
				wx.Frame.__init__(self, parent, -1, TTL_IDATA)
				self.SetWindowStyle(parent.GetWindowStyle())
				self.panel = wx.Panel(self, -1)
				self.method = method
				self.choices = choices

				sizer = wx.BoxSizer(wx.VERTICAL)

				self.collectionlabel = wx.StaticText(self.panel, label=LBL_DATASET)
				sizer.Add(self.collectionlabel, flag=wx.TOP | wx.LEFT | wx.RIGHT, border=15)

				choices = [option[0] for option in choices]
				self.collectioncombo = wx.ListBox(self.panel, choices=choices)
				self.collectioncombo.SetFocus()
				sizer.Add(self.collectioncombo, flag=wx.TOP | wx.LEFT | wx.RIGHT, border=15)

				connectbutton = wx.Button(self.panel, label=LBL_CONNECT)
				connectbutton.Bind(wx.EVT_BUTTON, self.connect)
				sizer.Add(connectbutton, flag=wx.TOP | wx.LEFT | wx.RIGHT | wx.BOTTOM, border=15)

				self.panel.SetSizerAndFit(sizer)
				self.Fit()

				self.Centre()
				self.Show()

			def connect(self, event=None):
				collection = self.collectioncombo.GetSelection()
				if collection != -1:
					name, actualcollection = self.choices[collection]
					self.method(actualcollection)
				self.Destroy()

		choices     = []
		collections = model.idata.get_list()["collections"]
		for collection in collections:
			choices.append([collection["name"], collection])

		IdataDialog(self, ctrl.start_idata, choices)

# ======================================================================================================
# === Controller classes  - manage options, run as app/demon/library, provide app logic for model & view
# ======================================================================================================

class Options(object):
	"""Store values derived from command line options"""
	def __init__(self, mode, allow, multi, suggest, workspacedir, daemon, stage, text, idataproj=None, idataprojdesc=None, workspacedesc=[], hidehome=False, hidesession=False, hidesdata=False, hideidata=False, filter=None, openremote="download"):
		global options
		options = self
		self.mode         = mode
		self.allow        = allow
		self.multi        = multi
		self.suggest      = suggest
		self.workspacedir = workspacedir
		self.daemon       = daemon
		self.stage        = stage
		self.text         = text
		self.idataproj    = idataproj
		self.idataprojdesc= idataprojdesc
		self.workspacedesc= workspacedesc
		self.hidehome     = hidehome
		self.hidesession  = hidesession
		self.hidesdata    = hidesdata
		self.hideidata    = hideidata
		self.filter       = filter
		self.openremote   = openremote

class Controller(object):

	Mode   = enum("Browse", "Open", "Save")
	Select = enum("Files", "Folders", "Both")
	Daemon = enum("NoDaemon", "Start", "Communicate", "Sync", "Stop")

	def __init__(self, options, replace_output_method=None, destroy_ui_at_finish=True):

		self.clipboard  = []
		self.cutting    = False
		self.showhidden = False
		self.extractors = [
					( '.tar.bz2','tar xjvf'  )
					,('.tar.gz' ,'tar xzvf'  )
					,('.bz2'    ,'bunzip2'   )
					,('.rar'    ,'rar x'     )
					,('.gz'     ,'gunzip'    )
					,('.tar'    ,'tar xf'    )
					,('.tbz2'   ,'tar xjvf'  )
					,('.tgz'    ,'tar xzvf'  )
					,('.zip'    ,'unzip -o'  )
					,('.Z'      ,'uncompress')
					,('.7z'     ,'7z x'      )
				]

		self.options              = options
		self.destroy_ui_at_finish = destroy_ui_at_finish
		self.output_method        = self.print_selected_items

		if replace_output_method:
			self.output_method = replace_output_method

		self.staged    = []  # list of temporary local files and their remote destinations
		self.savetimes = {}

		self.waitingprocs = []


	def trace(self, text):
		"""Print caller and text to stdout (for debugging)"""
		frame, filename, lineno, _, _, _ = inspect.stack()[1]
		sys.stderr.write(filename+':'+str(lineno)+' '+text+'\n')

	def print_selected_items(self, selection_all):
		if selection_all != None:
			for item in selection_all:
				actualoutputfile.write(item+'\n')

	def startup(self):
		self.restore(self.options)
		view.run()

	def exit(self, event=None):
		self.finish()

	def cut_items(self, event=None, items=None):
		self.cutting    = True
		self.clipboard = items

	def copy_items(self, event=None, items=None):
		self.cutting   = False
		self.clipboard = items

	def refresh(self):
		view.refresh_files()

	def paste_items(self, event=None, where=None):
		if where == None:
			where = self.get_path()
		view.status(STS_PASTE+str(self.clipboard)+STS_INTO+str(where))
		newdir = where
		for path in self.clipboard:
			newpath = newdir.join(path.basename())
			if newpath.exists():
				newpath = newdir.join('Copy of ' + path.basename())
			path.copy(newpath)
			if self.cutting:
				path.remove()
		self.refresh()

	def toggle_hidden(self):
		self.showhidden = not self.showhidden

	def extract(self, item=None, in_dir=None):
		itempath = item.path
		command  = None
		for ending, prg in self.extractors:
			if itempath.endswith(ending):
				command = prg
		in_dir.run_command(command.split(" ") + [None], files=[item], wait=True)

	def upload_file(self, into):
		into.upload(functools.partial(view.refresh_files, event=None, force=True))

	def download_file(self, items, path):
		for item in items:
			path.download(item)

	def open_item(self, item, path):
		path.run_command(["xdg-open", None], wait=False, files=[item], display=True)

	def new_folder(self, name, location):
		folderpath = location.join(name)
		folderpath.mkdir()

	def new_file(self, name, location):
		filepath = location.join(name)
		filepath.create()

	def delete_items(self, items):
		for item in items:
			item.remove()

	def rename_file(self, path, newpath):
		path.copy(newpath)
		path.remove()

	def compress(self, items, path, newname):
		newitem = path.join(newname)
		path.run_command(["zip", None] + [None]*len(items), files=[newitem] + items, wait=True)

	def start_ssh(self, hostname, username, password, location):
		path = model.start_ssh(hostname, username, password, location)
		view.foldertree.add_root_folder(path, expand=True, select=True, name=username + "@" + hostname + ":" + location)

	def start_idata(self, collection):
		path = model.start_idata(collection)
		view.foldertree.add_root_folder(path, expand=True, select=True, name="iData: " + collection["name"])

	def verify_selection(self, selection_all=None, use_dir=None, savename=None):
		if selection_all == None:
			selection_all = []
		verification = True

		# Open Mode
		if self.options.mode == Controller.Mode.Open:
			if self.options.allow == Controller.Select.Folders:
				if selection_all == [] and use_dir != None:
					selection_all = [use_dir]

			if self.options.multi == False and len(selection_all) > 1:
				return False, WRN_SELECT_SINGLE, selection_all

			hasfiles = any([item.isfile() for item in selection_all])
			hasdirs  = any([item.isdir()  for item in selection_all])

			if self.options.allow == Controller.Select.Files and hasdirs:
				return False, WRN_SELECT_FILE, selection_all
			elif self.options.allow == Controller.Select.Folders and hasfiles:
				return False, WRN_SELECT_FOLDER, selection_all

			if not all([item.exists() for item in selection_all]):
				return False, WRN_NOT_EXIST, selection_all

			if options.openremote == "unallowed" and any([item.is_remote for item in selection_all]):
				return False, "You cannot open a file on a remote file system! Please copy to a local filesystem and try again", selection_all

		# Save Mode
		elif self.options.mode == Controller.Mode.Save:
			if use_dir != None and not self.options.allow == Controller.Select.Folders:
				selection_all = [use_dir.join(basename) for basename in savename]

			if (not self.options.multi) and len(selection_all) > 1:
				return False, WRN_SELECT_SINGLE, selection_all

			if self.options.allow == Controller.Select.Files:
				if any([item.isdir() for item in selection_all]):
					return False, WRN_SELECT_FILE, selection_all

			if self.options.allow == Controller.Select.Folders:
				if any([item.isfile() for item in selection_all]):
					return False, WRN_SELECT_FOLDER, selection_all

			if any([item.exists() for item in selection_all]) and not self.options.allow == Controller.Select.Folders:
				if not view.ask(PMT_OVERWRITE_FILE):
					return False, None, selection_all

		return True, None, selection_all

	def get_temp_path(self, basename):
		new_file = model.tempdir.join(basename)
		if not new_file.exists() and not any(localfile == new_file for localfile, remotefile in self.staged):
			return new_file
		newtempdir = LocalFso(tempfile.mkdtemp())
		return newtempdir.join(basename)

	def finish(self, selection_all=None, use_dir=None, savename=None):
		for waitproc in self.waitingprocs:
			if waitproc.poll() == None:
				waitproc.kill()
		self.waitingprocs = []

		verified, errormessage, selection_all = self.verify_selection(selection_all=selection_all, use_dir=use_dir, savename=savename)

		if verified:
			if self.output_method != None:
				# make local paths
				localpaths = []
				for item in selection_all:
					if item.is_remote:
						if self.options.stage:
							new_file = self.get_temp_path(item.basename())
							if self.options.mode == Controller.Mode.Open:
								item.copy(new_file)
							localpaths.append(new_file.path)
							self.staged.append([new_file, item])
						else:
							if self.options.mode == Controller.Mode.Open:
								if options.openremote == "download":
									new_file = LocalFso(os.environ['HOME']).join("Downloads").join(item.basename())
									new_file.make_dirs()
									item.copy(new_file)
									localpaths.append(new_file.path)
								else:
									localpaths.append(item.remote_path())
							else:
								localpaths.append(item.remote_path())
					else:
							localpaths.append(item.path)

				self.output_method(localpaths)

			if self.destroy_ui_at_finish:
				view.Destroy()
			else:
				view.Hide()
				finishevent.set()

		elif errormessage != None:
				view.show_error(errormessage)

	def sync(self, error_method=False):
		errors = False
		try:
			Fso.set_cache_time_all()
			for localfile, remotefile in self.staged:
				lastsavetime = None
				if localfile in self.savetimes:
					lastsavetime = self.savetimes[localfile]
				if lastsavetime == None or localfile.mtime() > lastsavetime:
					if remotefile.exists():
						remotefile.remove()
					if localfile.exists():
						localfile.copy(remotefile)
					if not remotefile.exists():
						errors = True
					if errors:
						break
					self.savetimes[localfile] = localfile.mtime()
		except:
			errors = True

		if errors:
			try:
				if error_method != None:
					error_method()
			except:
				pass

		return errors

	def cleanup(self):
		self.sync()
		finishevent.set()
		model.cleanup()

	def restore(self, options):
		Fso.set_cache_time_all()
		self.options = options
		view.restore()

class ThreadManager(object):
	def __init__(self):
		atexit.register(self.onexit)

		self.output_method = None
		self.thread        = None
		self.files         = None

	def return_selected_items(self, files):
		self.files = files

	def startthread(self, options):
		self.thread = threading.Thread(target=functools.partial(self.main, options))
		self.thread.daemon = True
		self.cleanedup     = False
		self.thread.start()

	def run(self, options):
		self.startthread(options)
		self.wait()

	def runblocking(self, options):
		self.files      = None
		self.output_method  = self.return_selected_items
		finishevent.clear()
		if self.thread == None or not self.thread.is_alive():
			self.startthread(options)
		else:
			view.thread_request(functools.partial(ctrl.restore, options))
		finishevent.wait()
		return self.files

	def main(self, options):
		global ctrl, model, view

		ctrl  = Controller(options, replace_output_method=self.return_selected_items, destroy_ui_at_finish=False)
		model = Model()
		view  = View()
		try:
			ctrl.startup()
		finally:
			ctrl.cleanup()

	def wait(self):
		self.thread.join()

	def sync(self, showpopup=True):
		error_method = None
		if showpopup:
			error_method = functools.partial(view.thread_request, functools.partial(view.show_error, ERR_PROB_REMOTE_XFER))
		return ctrl.sync(error_method)

	def onexit(self):
		if self.thread != None:
			if self.thread.is_alive():
				view.thread_request(view.Destroy)
				self.wait()

# Global thread management for working with daemon

threadmgr   = None
finishevent = threading.Event()

def open_browser(options):
	global threadmgr
	threadmgr = create_thread()
	return threadmgr.runblocking(options)

def create_thread():
	global threadmgr
	if threadmgr == None:
		threadmgr = ThreadManager()
	return threadmgr

# Public functions

def manage(shortcut=None, text=None):
	return open_browser(Options(
		mode     = Controller.Mode.Browse,
		allow    = Controller.Select.Both,
		multi    = True,
		suggest  = '',
		workspacedir = shortcut,
		daemon   = Controller.Daemon.NoDaemon,
		stage    = False,
		text     = text))

def open_dialog(allow=Controller.Select.Files, stage=True, multi=True, suggest='', shortcut=None, text=None):
	return open_browser(Options(
		mode     = Controller.Mode.Open,
		allow    = allow,
		multi    = multi,
		suggest  = suggest,
		workspacedir = shortcut,
		daemon   = Controller.Daemon.NoDaemon,
		stage    = stage,
		text     = text))

def save_dialog(allow=Controller.Select.Files, stage=True, multi=False, suggest="NewFile", shortcut=None, text=None):
	return open_browser(Options(
		mode     = Controller.Mode.Save,
		allow    = allow,
		multi    = multi,
		suggest  = suggest,
		workspacedir = shortcut,
		daemon   = Controller.Daemon.NoDaemon,
		stage    = stage,
		text     = text))

def sync(showpopup=True):
	return threadmgr.sync(showpopup=showpopup)

# Global MVC objects

ctrl  = None
model = None
view  = None

if __name__ == '__main__':

	# STEP 1: Determine options selected by caller

	# Get values and handle help text
	parser = argparse.ArgumentParser(description=HLP_DESCRIPTION, formatter_class=argparse.RawTextHelpFormatter)
	parser.add_argument('--shortcut',dest='workspace',metavar="PATH"                             ,default=None       ,help=HLP_WORKSPACEDIR, action='append')
	parser.add_argument('--shortcutdesc',dest='workspacedesc',metavar="PATH"                     ,default=None       ,help=HLP_WORKSPACEDESC, action='append')
	parser.add_argument('--idataproject',dest='idataproj',metavar="TEXT"                         ,default=None       ,help=HLP_IDATAPROJECT)
	parser.add_argument('--idataprojectdesc',dest='idataprojdesc',metavar="TEXT"                 ,default=None       ,help=HLP_IDATAPROJECTDESC)
	parser.add_argument('--filter-extension',dest='filter',metavar="TEXT"                        ,default=None       ,help=HLP_FILTER, action='append')
	parser.add_argument('--instruct',dest='instruct' ,metavar="TEXT"                             ,default=None       ,help=HLP_INSTRUCT)
	parser.add_argument('--open'    ,dest='openopt'                                              ,action="store_true",help=HLP_OPEN)
	parser.add_argument('--open-remote-paths' ,dest='openremote',choices=["download", "unallowed", "allowed"],default="download",help=HLP_OPEN_REMOTE)
	parser.add_argument('--save'    ,dest='saveopt'                                              ,action="store_true",help=HLP_SAVE)
	parser.add_argument('--multiple',dest='multiple'                                             ,action="store_true",help=HLP_MULTIPLE)
	parser.add_argument('--files'   ,dest='files'                                                ,action="store_true",help=HLP_FILES)
	parser.add_argument('--folders' ,dest='folders'                                              ,action="store_true",help=HLP_FOLDERS)
	parser.add_argument('--suggest' ,dest='suggest'  ,metavar="FILENAME"                         ,default="NewItem"  ,help=HLP_SUGGEST)
	parser.add_argument('--daemon'  ,dest='daemon'   ,choices=["start", "appear", "sync", "stop"],default="nodaemon" ,help=HLP_DAEMON)
	parser.add_argument('--stage'   ,dest='stage'                                                ,action="store_true",help=HLP_STAGE)
	parser.add_argument('--hide-home-dir' ,dest='hidehome'                                       ,action="store_true",help=HLP_HIDE_HOME)
	parser.add_argument('--hide-session-dir' ,dest='hidesession'                                 ,action="store_true",help=HLP_HIDE_SESSION)
	parser.add_argument('--hide-sdata-dir' ,dest='hidesdata'                                     ,action="store_true",help=HLP_HIDE_SDATA)
	parser.add_argument('--hide-idata-projects-browser' ,dest='hideidata'                        ,action="store_true",help=HLP_HIDE_IDATA)
	args = parser.parse_args(sys.argv[1:])

	# STEP 2: Build object to track options

	mode = Controller.Mode.Browse
	if   args.openopt: mode = Controller.Mode.Open
	elif args.saveopt: mode = Controller.Mode.Save

	allow = Controller.Select.Both
	if   args.files:   allow = Controller.Select.Files
	elif args.folders: allow = Controller.Select.Folders

	daemon = Controller.Daemon.NoDaemon
	if   args.daemon == "start":  daemon = Controller.Daemon.Start
	elif args.daemon == "appear": daemon = Controller.Daemon.Communicate
	elif args.daemon == "stop":   daemon = Controller.Daemon.Stop
	elif args.daemon == "sync":   daemon = Controller.Daemon.Sync

	stage = False
	if   args.stage and daemon != Controller.Daemon.NoDaemon: stage = True;

	options = Options(mode, allow, args.multiple, args.suggest, args.workspace, daemon, stage, args.instruct, args.idataproj, args.idataprojdesc, args.workspacedesc, args.hidehome, args.hidesession, args.hidesdata, args.hideidata, args.filter, args.openremote)

	# STEP 3: Run as normal process or as deamon process

	address = ('localhost', 6001)     # family is deduced to be 'AF_INET'
	authkey = 'filebrowserkey'

	# Normal - run as dektop application
	if options.daemon == Controller.Daemon.NoDaemon:
		# NOTE: Avoiding use of thread here makes it easier to run debugger
		ctrl  = Controller(options)
		model = Model()
		view  = View()
		try:
			ctrl.startup()
		finally:
			ctrl.cleanup()

	# Launch deamon in background process
	elif options.daemon == Controller.Daemon.Start:
		threadmgr = create_thread()
		listener  = Listener(address, authkey=authkey)
		while True:
			conn = listener.accept()
			msg  = conn.recv()
			options = pickle.loads(msg)
			if options.daemon == Controller.Daemon.Communicate:
				returnvalue = open_browser(options)
			elif options.daemon == Controller.Daemon.Sync:
				returnvalue = sync()
			elif threadmgr != None:
				threadmgr.onexit()
				returnvalue = None
			conn.send(pickle.dumps(returnvalue))
			conn.close()
			if options.daemon == Controller.Daemon.Stop:
				break
		listener.close()

	# Interact with existing damon
	elif options.daemon == Controller.Daemon.Communicate or \
		 options.daemon == Controller.Daemon.Stop        or \
		 options.daemon == Controller.Daemon.Sync:
		# TODO Gracefully handle case where daemon does not exist
		conn = Client(address, authkey=authkey)
		conn.send(pickle.dumps(options))
		msg = conn.recv()
		returnvalue = pickle.loads(msg)
		conn.close()
		if options.daemon == Controller.Daemon.Sync:
			errors = returnvalue
			if errors:
				sys.stdout.write('Error\n')
			else:
				sys.stdout.write('No errors\n')
		elif returnvalue != None:   # Pass along selected items
			for item in returnvalue:
				sys.stdout.write(item+'\n')
