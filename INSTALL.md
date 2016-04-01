# OwnTrack - Installation guide
This guide will explain all steps to set up a working OwnTrack installation. For this guide we assume you have already installed the OwnTrack app on your smartphone. If you don't know how to install the app, check [this](/README.md#howto---install-the-client) chapter.

## Video HowTo
On youtube: https://www.youtube.com/watch?v=RQiMUfzfB94

## Preparation
To set up OwnTrack you will need a server, with important abilities. You don't have to use an expensive root server for this. You can use any server which is able to:
- Execute PHP scripts
- Manage MySQL databases
- Handle HTTPS requests (Self signed certificates may be allowed trough app settings)
- (Optional) Execute python scripts

Almost any server on the web would be able to execute this tasks. If you really don't want to spend any money, you can also use a home server for this. In this case its important to have a static IP or use dynamic DNS, which can be found for free on the web (e.g. [selfhost](https://cms.selfhost.de/cgi-bin/selfhost?p=cms&article=free) or [noip](https://www.noip.com/)). But this should not be a tutorial, on how to setup a generic server, you can find enough of them on the internet. Just a small tip, you could use XAMPP on Windows (But should be aware of the risks of having XMPP accessible from the internet), but it is highly recommended to use a linux server (Ubuntu and Debian are pretty reliable as server OS). If you do not want to change to linux, just run the server in a virtual machine.

We assume that you have now either a running Debian or Ubuntu system. Now we need to install a few applications to meet our requirements.

If you are not that used to linux it is recommended to install following applications:
- apache2 as web server, which is also easily set up to support HTTPS
- mysql-server as mysql server
- php5 to handle php scripts
- php5-mysql to communicate between php and MySQL
- phpMyAdmin as a comfortable web GUI to manage MySQL databases
- python to draw the location history on a map
- python-mysqldb to communicate between python and MySQL

For further installation instructions for these applications, just google the name and you should find plenty of tutorials.

## Configure software
If the previous software is now set up correctly. (Have an eye on our requirements), you should now create a new mysql user (optional but for security concerns recommended - we assume you created the user otuser from now on) with just one database (we assume it is called "owntrack" from now on).

##Initialize database
We now initialize the database structure for OwnTrack. If you have installed phpMyAdmin, login with otuser and your __save__ password. Select the owntrack database, click on execute SQL command and paste the content of the [/server/newdb.sql](/server/newdb.sql) file in the text box and execute. If everything worked as expected you should now have two new tables in the owntrack database: location_history and used_salt. If not you did something wrong and should google the error message and check everything again.

## Upload latest php scripts
These scripts, handle the locations sent from the app. You should upload both server scripts in [/server/public/](/server/public/) (commit.php and config.php) to the, from the web accessible folder on your server. (Often "/srv/www/" or "/var/www") You can rename "commit.php" to whatever you like, but keep the ".php" and remember that you have to enter that filename later in the app.

## Upload latest python script
This script will later visualize the location history. Upload the latest [/server/private/draw_map.py](/server/private/draw_map.py) to a __private__, _not_ from the web accessible folder. Theoretically you could use this script also on your normal computer, but most mysql installations are _for a good reason_ only accessible locally and you should leave it that way, to keep the attack surface as small as possible.

## Setup configuration
Open the php script "config.php" in the web folder and the python script "draw_map.py" in the private folder and change the settings accordingly to your setup. "config.php" should have enough comments (_Especially important is the comment above $commonSecret_) , in the "draw_map.py" script the settings are "hidden" in Line 54.

Lets say, we have now chosen the strong common secret "IOsaSIOs4a0f45_4sasad" with a good head on the keyboard roll, our mysql installation is on the same host as both scripts, our mysql username is "otuser", table is "owntrack" and the mysql password is "zd1uO8sa4d4F0gu-zuApqz_ogu". (Please do not use this password and secret)

The python config should then look like this:
```python
db = MySQLdb.connect(host	= "localhost", # your host, usually localhost
                 user		= "otuser", # your username
                 passwd		= "zd1uO8sa4d4F0gu-zuApqz_ogu",	# your password
	        	 db			= "owntrack") # name of the data base

```

The php config should look then look like this:
```php
$commonSecret = "IOsaSIOs4a0f45_4sasad";
$hostServer = "localhost";
$databaseUser = "otuser";
$databasePassword = "zd1uO8sa4d4F0gu-zuApqz_ogu";
$tableName = "owntrack";
?>
```

## Thats it!
You have now hopefully setup your server successfully. At last, open the app on your phone. URL is the from the web accessible URL of the file "commit.php" (Or what you have renamed it to).

If you have no HTTPS certificate (If this is the case [Let's Encrypt](https://letsencrypt.org/) is highly recommended) you now need to allow self signing and pin the certificate. If you do this, be _sure_ the fingerprint is correct.

Now enter the commonSecret you entered in the "config.php" and the if everything went fine, the word "Working" will appear in the server settings.

## Screenshots

### Location history uploaded successfully, here viewed with phpMyAdmin
<img align="left" src="/screenshots/server/2016-03-17_db.png?raw=true" width="50%" />
