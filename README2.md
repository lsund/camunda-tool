camunda-tool
============

[NAME](#NAME)  
[SYNOPSIS](#SYNOPSIS)  
[DESCRIPTION](#DESCRIPTION)  
[COMMANDS](#COMMANDS)  
[OPTIONS](#OPTIONS)  
[BUGS](#BUGS)  
[AUTHOR](#AUTHOR)  

------------------------------------------------------------------------

NAME <span id="NAME"></span>
----------------------------

camunda-tool − useful wrapper around the Camunda REST API

SYNOPSIS <span id="SYNOPSIS"></span>
------------------------------------

**camunda-tool COMMANDS OPTIONS**

DESCRIPTION <span id="DESCRIPTION"></span>
------------------------------------------

**camunda-tool** is a wrapper around the Camunda REST API for
interacting with Camunda BPM from the command line. Designed to give
easy and fast access to the core functionality you need to develop and
monitor camunda processes.

COMMANDS <span id="COMMANDS"></span>
------------------------------------

COMMANDS is a list of tokens for commanding camunda tool

Valid COMMANDS are:

<table>
<tbody>
<tr class="odd">
<td></td>
<td><p>list</p></td>
<td></td>
<td><p>lists all currently running process instances</p></td>
<td></td>
</tr>
</tbody>
</table>

list &lt;definition&gt;

lists all currently running process instances of the given definition

<table>
<tbody>
<tr class="odd">
<td></td>
<td><p>hlist</p></td>
<td></td>
<td><p>lists all process instances</p></td>
<td></td>
</tr>
</tbody>
</table>

hlist &lt;definition-key&gt;

lists all process instances of the given definition

vars &lt;process-id&gt;

list the variables of the given process ID.

start &lt;definition-key&gt; &lt;variables&gt;

starts an instance of the latest process definition with the given
definition key given start variables.

monitor &lt;process-id&gt;  
input-vars &lt;definition-key&gt;

OPTIONS <span id="OPTIONS"></span>
----------------------------------

The nuseradd does not take any options. However, you can supply
username.

BUGS <span id="BUGS"></span>
----------------------------

No known bugs.

AUTHOR <span id="AUTHOR"></span>
--------------------------------

Ludvig SundstrÃ¶m (lud.sund@gmail.com)

------------------------------------------------------------------------
