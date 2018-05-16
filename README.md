# Fastmail
Fast Java Mail Client

-----
POLL:
I had been using Thunderbird for years and really liked that mail client. In the 
past years I found two things annoying:

1) mail synchronization is extremely slow, at least if you use the client the
way I do

2) the GUI is sluggish, and sometimes hangs (mostly when the synchronization runs)

The more I worked with javamail, and the more I got into the topic of mail 
delivery and management, the more I learned that the IMAP protocol is the
real bottleneck for slow mail clients. While doing my refactoring I try to 
address the performance of mail communication and see one issue: at least
javamail does not support the mail ID within the mail header. (At least there
is no way in javamail to get this mail ID.) Additionally I am still unsure, if
this mail ID is really unique and one can really rely on its uniqueness. I
believe relying on it does not work, otherwise clients like Thunderbird had
the possibility to get the synchronization between local and server side done
the right way.

As far as I have not read any feedback about Fastmail by now and the mail
protocol works the way it does, I believe a real performance enhancement
compared to Thunderbird is not realistic. Additionally Thunderbird implements
features, which I would need years to realize.
Thus I am thinking about dropping this project.
 
If you are (still) interested in this mail client, please drop me an email
at jochen.rassler_at_gmail.com. Getting enough feedback will motivate me to
continue this project for sure..... but right now I am still working without
any user feedback.


----

Fastmail is a Java Email Client, that uses 
JavaMail ( https://java.net/projects/javamail/pages/Home )
for email connection to server. The GUI is build using JavaFX.

I wrote this peace of software as I had been in need of a new Email client for
my new Laptop. The available Email clients didn't fit my needs so I decided to write
one to suit my needs.

I am developing on FreeBSD using OpenJDK. 
So please provide me with feedback for other platforms and compilers.


Version history:
please see HISTORY file

Project roadmap:
please see ROADMAP file

Current status:
I currently refactor the architecture of this mail client to get a better 
basis for more features as the GUI code and mail code currently are coupled
to strong. The development is done in branch 'refactoring', but not always
runnable. So please be patient for the next release.
