## Transmitter

Repo for Jetbrains plugin **Transmitter**. Transmitter is a simple transmission tool based on rsync for sending your project to remote locations.

- Supported systems: Mac and Linux.
- Using a passwordless ssh key is recommended.
- To use password, `sshpass` needs to be installed.
- `Exclude` accepts a comma separated list of files/dirs.
- [Plugin homepage](https://plugins.jetbrains.com/plugin/13708-transmitter)

#### Usage
1. Configure connections in the menu Tools -> Transmitter Configuration

2. Select one of the connections to be project default via checkmark button

3. Send project using "Transmit project to Default Location" in the context menu

#### Screenshots
![config_menu](https://github.com/piekill/transmitter/blob/master/screenshots/config_menu.png?raw=true "Configuration Menu")
![config_dialog](https://github.com/piekill/transmitter/blob/master/screenshots/config_dialog.png?raw=true "Configuration Dialog")
![transmit_menu](https://github.com/piekill/transmitter/blob/master/screenshots/transmit_menu.png?raw=true "Transmit Menu")
