# Fastmail
Fast Java Mail Client

Fastmail is a Java Email Client, that uses 
JavaMail ( https://java.net/projects/javamail/pages/Home )
for email connection to server.
JavaMail needs Java Activation Framework, jaf-1.1.1, so you most probably need to install 
that, too.

I wrote this peace of software as I had been in need of a new Email client for
my new Laptop. The available Email clients didn't fit my needs so I decided to write
on to suit my needs.

I am developing on FreeBSD using OpenJDK. 
So please provide me with feedback for other platforms and compilers.

Version 0.0.4 (HEAD)
* context menu for mails with functionality, to reply, delete and move messages
* externalized strings, preparation for internationalization
* i18n Packages for en_US and de_DE added
* better support of lazy loading (as intended by JavaMail)
* mail folders now show their subfolders

Version 0.0.3
* redesigned Mail and Folder integration to get a more flexible and straightforward design
* Fastmail now can rename email folders
* INBOX synchronizes on a regular basis
* every time you choose a folder, it is synchronized
* mails will be sent in an own thread, so GUI is available immediately 
* attachments will be saved in an own thread, so GUI is available immediately
* folders can be added and deleted using tree view context menu


Version 0.0.2
* added functionality to remove mail account
* added functionality to edit an existing account
* added ReplyAll
* added functionality to save and save all attachments
* added functionality to attach files to mails
* sent mails now show the configured display name


Version 0.0.1
* write emails
* read emails
* reply to emails
* use several email accounts
* INBOX is always stored in memory and tracked for new mails
* Fastmail currently works completely online, so no local data holding
	(except for the account configuration, which is encoded)
* Fastmail makes excessive use of multithreading for faster email processing

