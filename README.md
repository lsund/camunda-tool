# Camunda Tool

A command line utility for using Camunda BPM. It is essentially a wrapper
around the [Camunda REST
API](https://docs.camunda.org/manual/7.11/reference/rest/), allowing convenient
access to the most important functionality when developing with Camunda.

To build and install, call the `shell/install` script:
```
# Builds the program and copies an executable it into $HOME/.local/bin
$ ./shell/install
# Run the camunda-tool or add it to your path
$ $HOME/.local/bin/camunda-tool list
```

For usage, please see [the manpage](man/camunda-tool.1).
```
$ man ./man/camunda-tool.1
```
