# Fastmail
Fast Java Mail Client

Fastmail is a Java Email Client, that uses 
JavaMail ( https://java.net/projects/javamail/pages/Home )
for email connection to server.

I wrote this peace of software as I had been in need of a new Email client for
my new Laptop. The available Email clients didn't fit my needs so I decided to write
on to suit my needs.

I am developing on FreeBSD using OpenJDK. 
So please provide me with feedback for other platforms and compilers.

Version 0.0.2
* added functionality to remove mail account
* added functionality to edit an existing account

Version 0.0.1
* write emails
* read emails
* reply to emails
* use several email accounts
* INBOX is always stored in memory and tracked for new mails
* Fastmail currently works completely online, so no local data holding
	(except for the account configuration, which is encoded)
* Fastmail makes excessive use of multithreading for faster email processing

