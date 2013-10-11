Enkive is an Email archival and discovery solution written in Java.  It
supports policy-based retention (including time-based and size-based limits),
de-duplication of email and attachmets, full content and metadata searching,
and access via IMAP.

Email can be archived via a direct filter tap in MTAs such as Postfix, via IMAP
or POP polling by the Enkive server, by forwarding to a dedicated Postfix
install, or by a custom tap written for the target system.

Email is stored in MongoDB and indexed by INDRI.

IMPORTANT

If you are upgrading an existing installation of Enkive, you should
read UPGRADE.txt to understand the process.

DOCUMENTATION

This is an installation of the Enkive email archiver Community
Edition. Documentation can be found at:

    http://wiki.enkive.org/index.php/Main_Page

LICENSES

Enkive free software, under the terms of the GNU AGPLv3.  See the "licenses"
subdirectory for more licensing information.

UPGRADE CHECKING

As of version 1.4, Enkive will periodically (every 2 weeks by default) check to
see if there is a new version available.  This check sends the current version
and gets back the most recent version.  If there is a new version, a notice
will be displayed on the "System Administration" page.

This feature can be configured or disabled entirely via the
"enkive.administration.*" settings in enkive.properties.
