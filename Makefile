all: build copy

build:
	gradle build

copy:
	cp "/Users/artemapostatov/hs/Minecraft-1.16.5/build/libs/tutorialmod-1.16.5-1.0.jar" "/Users/artemapostatov/Library/Application Support/tlauncher/legacy/mods/"
