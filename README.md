# Log Cleaner

A lightweight mod that cleans old, unused log files.

The best thing is that this mod keeps files not based on creation date, but the most recent access (of any kind) to the file, so logs that are 
still being accessed for whatever reason are not removed unwillingly.

It works both client-side only and server-side only, and works in every Minecraft version.

By default, it will clean logs that haven't been used in more than 14 days, so there's plenty of time in case you ever need them for debugging,
but you can configure it to any amount of days you want.

## Configuration

You can find the mod's configuration file in `config/logcleaner.json`. There's two options you can change in there:

- `daysOld`: The number of days a log has to have been unused in order for it to be deleted. Defaults to `14`
- `silent`: If set to `true`, Log Cleaner will not send a message with the amount of deleted log files to the log. Defaults to `false`

## Setup

For setup instructions please see the [fabric wiki page](https://fabricmc.net/wiki/tutorial:setup) that relates to the IDE that you are using.

Releases have their json files minified and are recompressed further.
