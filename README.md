# TopTime
A simple top-list plugin to easily show player's playtime

## Planned features:
/toptime [timeFormat], shows the players ordered by playtime

/toptime {pageNo} [timeFormat], shows the pageNo page. For example /toptime 3 shows the third toplist page, players 31st to 40th

/gettime [timeFormat], shows player own playtime.

/gettime {playerName} [timeFormat], shows the playtime of selected player.

/gettime total [TimeFormat], shows the combined playtime on the server.

Visualize the playtime in user selected format:
- "", Empty string: normal format = 00y00Mo00d00h00m00s000ms, Redundant zeros will be removed. E.g., 6Mo22d50m53s10ms (missing years and hours)
- "y" OR "years": displays the playtime in years
- "mo" OR "months": displays the playtime in months
- "d" OR "days": displays the playtime in days
- "h" OR "hours": displays the playtime in hours
- "s" OR "seconds": displays the playtime in seconds
- "ms" OR "milliseconds": displays the playtime in milliseconds
- "t" OR "ticks": displays the playtime in game ticks
  
  Language file support.
