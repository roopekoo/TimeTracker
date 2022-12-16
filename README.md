# TimeTracker
A simple top-list plugin to easily show player's playtime. Order the players based on the playtime and convert & display the playtimes to different timeformats. Live update the players playtime that are online.

## Features:
- /toptime [timeFormat], shows the players ordered by playtime
- /toptime {pageNo} [timeFormat], shows the pageNo page. For example /toptime 2 shows the third toplist page, players 11. to 20.
![](https://i.imgur.com/2y3Mq4l.png "Page 2 of the playtime toplist")
- /toptime force, force updates and orders the playtime list
- /gettime [timeFormat], shows player own playtime.
- /gettime {playerName} [timeFormat], shows the playtime of selected player.
![](https://i.imgur.com/3MFqAPZ.png "Player specific playtime")
- /gettime total [TimeFormat], shows the combined playtime on the server.
![](https://i.imgur.com/ag6CF0J.png "All players total playtime without and with format 'h'")

Visualize the playtime in user selected format:
- "", Empty string: normal format = 00y00Mo00d00h00m00s000ms; Redundant zeros will be removed. E.g., 6Mo22d50m53s10ms (missing years and hours)
- "y" OR "years": displays the playtime in years
- "mo" OR "months": displays the playtime in months
- "d" OR "days": displays the playtime in days
- "h" OR "hours": displays the playtime in hours
- "s" OR "seconds": displays the playtime in seconds
- "ms" OR "milliseconds": displays the playtime in milliseconds
- "t" OR "ticks": displays the playtime in game ticks
  
  Language file support. You can translate the sentences to your own language.
  
  Support for command permissions. Allowed by default for all players except the /toptime force command.
  
  Support for tab completition.
