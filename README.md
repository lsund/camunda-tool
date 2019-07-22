# Camunda Tool

A command line utility for using Camunda BPM. It wraps around the [Camunda REST
API](https://docs.camunda.org/manual/7.11/reference/rest/), exposing a
convenient interface to some of the most important queries.

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
