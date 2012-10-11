<a href="http://projectdanube.org/" target="_blank"><img src="http://peacekeeper.github.com/xdi2/images/projectdanube_logo.png" align="right"></a>
<img src="http://peacekeeper.github.com/xdi2/images/logo64.png"><br>

This is a connector module for the [XDI2](http://github.com/peacekeeper/xdi2) server.

It can map personal data from the [Allfiled API](http://allfiled.com/) to XDI. 

### Information

* [Notes](https://github.com/peacekeeper/xdi2-connector-allfiled/wiki/Notes)
* [Mapping](https://github.com/peacekeeper/xdi2-connector-allfiled/wiki/Mapping)
* [Sequences](https://github.com/peacekeeper/xdi2-connector-allfiled/wiki/Sequences)

### How to build

First, you need to build the main [XDI2](http://github.com/peacekeeper/xdi2) project.

After that, just run

    mvn clean install

To build all components.

### How to run

    mvn jetty:run

Then access the web interface at

	http://localhost:9090/

Or use an XDI client to send XDI messages to

    http://localhost:9090/xdi/allfiled

### How to build as XDI2 plugin

Run

    mvn clean install package -P xdi2-plugin

### Community

Google Group: http://groups.google.com/group/xdi2
