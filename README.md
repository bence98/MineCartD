# MineCartD
A Linux daemon for managing Minecraft servers


## Usage
On the server, run `minecartd`.
Then on the client, connect to it with an appropriate tool (ex. `telnet`) to access the Command Interface


## Setting up the server

### Config
The config file consists of `key=value` pairs. Lines starting with `#` won't be interpreted. If the config file doesn't exist, `minecartd` will attempt to create it.
#### Settings:
* `dir`: The directory holding the servers' directories. Default: `/var/lib/minecartd/servers`
* `port`: The TCP port for the Command Interface. Default: `40960`

### Command-line parameters
* `--cfgfile|-f <file>`: use this config file instead of `/etc/minecartd.conf`
* `--gen-cfg|-C`: generate config and quit. Won't launch Command Interface. Can't be used with `-S`
* `--stop|-S`: connect to a server on localhost and send STOP to it (stops all Minecraft servers and the `minecartd` host). Can't be used with `-C`


## Using the Command Interface on the client
When connection is established to the `minecartd` server, you'll see a welcome prompt telling the server's `minecartd` version. If there's no prompt, that means there's a connection failure. Check your connection as well as verify that the server is up and running.

### Command Interface commands
* `help`: Lists all commands
* `list`: Lists all servers
* `STOP`: Stops all servers and quits
* `start <server>`: Starts the Minecraft server
* `kill <server>`: Kills the Minecraft server. Use only when the server is beyond-recoverable frozen. You may lose your world! You have been warned.
* `cmd <server> stop`: The proper way of closing the Minecraft server
* `cmd <server> <command>`: Execute a Minecraft command as an Admin
* `log <server> [ln]`: Outputs the last `ln` lines (default: 10, max: 127) of the output of the Minecraft server
