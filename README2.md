camunda-tool {#camunda-tool align="center"}
============

[NAME](#NAME)\
[SYNOPSIS](#SYNOPSIS)\
[DESCRIPTION](#DESCRIPTION)\
[COMMANDS](#COMMANDS)\
[OPTIONS](#OPTIONS)\
[BUGS](#BUGS)\
[AUTHOR](#AUTHOR)\

------------------------------------------------------------------------

NAME []{#NAME}
--------------

camunda-tool − useful wrapper around the Camunda REST API

SYNOPSIS []{#SYNOPSIS}
----------------------

**camunda-tool COMMANDS OPTIONS**

DESCRIPTION []{#DESCRIPTION}
----------------------------

**camunda-tool** is a wrapper around the Camunda REST API for
interacting with Camunda BPM from the command line. Designed to give
easy and fast access to the core functionality you need to develop and
monitor camunda processes.

COMMANDS []{#COMMANDS}
----------------------

COMMANDS is a list of tokens for commanding camunda tool

Valid COMMANDS are:

  -- ------ -- ----------------------------------------------- --
     list      lists all currently running process instances   
  -- ------ -- ----------------------------------------------- --

list \<definition\>

lists all currently running process instances of the given definition

  -- ------- -- ----------------------------- --
     hlist      lists all process instances   
  -- ------- -- ----------------------------- --

hlist \<definition-key\>

lists all process instances of the given definition

vars \<process-id\>

list the variables of the given process ID.

start \<definition-key\> \<variables\>

starts an instance of the latest process definition with the given
definition key given start variables.

monitor \<process-id\>\
input-vars \<definition-key\>

OPTIONS []{#OPTIONS}
--------------------

The nuseradd does not take any options. However, you can supply
username.

BUGS []{#BUGS}
--------------

No known bugs.

AUTHOR []{#AUTHOR}
------------------

Ludvig SundstrÃ¶m (lud.sund\@gmail.com)

------------------------------------------------------------------------
