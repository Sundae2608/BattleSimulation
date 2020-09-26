This package contains whole configs, meaning that each folder in here contains all different config
to make a game session. This include:
* Audio config
* Battle config
* Construct config
* Graphic config
* Map config
* Surface config

This is quite unsustainable in the long-term. The goal should be that all config
reside in one single .json file, and use our JSONReader library. This way, config is easily copiable and transferable.

This whole config will also not include GameConfig, which is standalone since GameConfig deals more with the balancing
of different unit in the game than the actual game itself.