#!/usr/bin/python
import MySQLdb
import sys
import urllib
from time import strftime
from datetime import datetime

import Image, ImageDraw
import math

def latDegToPx(deg, lat):
        global zoom
        return (float(111000) / ((6372798.2 * 2 * math.pi) * (math.cos(lat * (math.pi / 180)) / (math.pow(2, (zoom + 8)))))) * deg

def lonDegToPx(lon):
        global zoom
        return (lon * 1024) / ((float(1) / (float(pow(2, zoom)) / 180)) * 4 * 2)

def drawPoint(lat,lon):
        global mlat, mlon, draw

        x = (1024 / 2) + lonDegToPx(lon - mlon)
        y = (1024 / 2) - latDegToPx(lat - mlat, mlat)

        r = 2
        draw.ellipse((x - r, y - r, x + r, y + r), fill = 'red')

def show_help():
        print "Usage: python export_gpx.py <filename> [<from_date> <to_date>]"
        print "Example: python export_gpx.py export.gpx 2015-01-01 2015-31-12"
        print "Example: python export_gpx.py export.gpx '2015-01-01 12:00:00' '2015-12-31 13:15:00'"
        sys.exit()

argv = sys.argv
argc = len(argv)
usefile = False
usedate = False
if (argc == 2 and argv[1] == "help"):
        show_help()
elif (argc == 2):
        usefile = True
        filename = argv[1]
elif (argc == 3):
        usedate = True
        date_from = argv[1]
        date_to = argv[2]
elif (argc == 4):
        usefile = True
        usedate = True
        filename = argv[1]
        date_from = argv[2]
        date_to = argv[3]
elif (argc != 1):
        show_help()

db = MySQLdb.connect(host = "",    # your host, usually localhost
                     user = "",         # your username
                     passwd = "",  # your password
                     db = "")        # name of the data base

# you must create a Cursor object. It will let
#  you execute all the queries you need
cur = db.cursor()

# Use all the SQL you like
if (usedate):
        query = "SELECT * FROM `location_history` WHERE "\
                "`timestamp` BETWEEN '%s' AND '%s'" % (date_from, date_to);
else:
        query = "SELECT * FROM `location_history`"

cur.execute(query)

# print all the first cell of all the rows
result = cur.fetchall()

#download map
minLat, maxLat = 100, -100
minLon, maxLon = 200, -200
for l in result:
        if (l[1] < minLat):
                minLat = l[1]

        if (l[1] > maxLat):
                maxLat = l[1]

        if (l[2] < minLon):
                minLon = l[2]

        if (l[2] > maxLon):
                maxLon = l[2]

mlon = (minLon + maxLon) / 2
mlat = (minLat + maxLat) / 2

zoom = int(math.log((360 / ((maxLat - minLat) / 1)),2))

maptemp = "map.png.temp"
urllib.urlretrieve("http://staticmap.openstreetmap.de/staticmap.php?center=" + str(mlat) + "," + str(mlon) + "&zoom=" + str(zoom) + "&size=1024x1024&maptype=mapnik", maptemp)

#create file
if (usefile):
        f = open(filename, 'w+')
        f.write('<?xml version="1.0" encoding="UTF-8" ?>\n'
                '<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="custom" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd ">\n'
                '<trk>\n'
                '<name><![CDATA[Custom]]></name>\n'
                '<trkseg>')

i = 0
im = Image.open(maptemp)#h,w = 1024
draw = ImageDraw.Draw(im)
for l in result:
        i += 1
        d = l[0].strftime("%Y-%m-%dT%H:%M:%SZ")

        #print "time %s - lat %s - lon %s - acc %s - spd %s - prv %s" % (l[0], l[1], l[2], l[3], l[4], l[5])

        drawPoint(l[1], l[2])

        if (usefile):
                s =     '<trkpt lat="%s" lon="%s">\n'\
                        '<time>%s</time>'\
                        '<extensions>'\
                        '<speed>%s</speed>'\
                        '</extensions>'\
                        '</trkpt>' % (l[1], l[2], d, l[4])
                f.write(s)

del draw
im.save("map.png", "PNG")
print "drawn to map.png"


if (usefile):
        f.write('</trkseg></trk></gpx>')
        f.close()

print "written %d lines" % i
