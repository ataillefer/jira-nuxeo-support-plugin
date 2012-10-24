# JIRA plugin for Nuxeo

Plugin for JIRA to attach to an issue a ZIP containing useful information fetched from a Nuxeo server, such as:

- system information (OS, JVM)
- Nuxeo distribution
- installed marketplace packages
- Nuxeo configuration (nuxeo.conf)
- server logs (server.log)

## Principle

### Nuxeo side

Exposes a service through a JAX-RS resource that allows to download the ZIP file.
The ZIP structure is:

server_info.zip
  |__ system.info
  |__ distrib.info
  |__ packages.info
  |__ nuxeo.conf
  |__ server.log

### JIRA side

Plugin that adds custom fields on the issue form including: server URL, user name, password and a button to collect the information from the Nuxeo server.
