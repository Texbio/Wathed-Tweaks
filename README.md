### Normal Room Numbers
- `/wathe:rooms setmax <0-255>` — sets how many standard rooms (`Room 1` through `Room N`) are given out at game start. `0` disables standard rooms entirely, leaving only custom rooms if enabled.
- `/wathe:rooms giveRoomKey <name or number>` — gives a key to yourself. If the input is a plain integer, `"Room "` is prepended automatically. Otherwise the name is used as-is. e.g. `giveRoomKey 1` → `"Room 1"`, `giveRoomKey Suite A` → `"Suite A"`.
### Custom Rooms
- `/wathe:rooms customadd <name>` — adds a room name to the custom list. No auto-formatting — type the full name exactly as you want it. e.g. `customadd Room 8` adds `"Room 8"`.
- `/wathe:rooms customremove <name>` — removes a room from the custom list by exact name.
- `/wathe:rooms customlist` — lists all custom rooms (numbered) and shows whether custom giving is currently enabled or disabled.
- `/wathe:rooms customtogglegive` — toggles whether custom rooms are included in key distribution at game start. Off by default.
