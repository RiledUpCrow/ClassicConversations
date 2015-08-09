# BetonQuest Classic Conversations

BQCC is an add-on for BetonQuest plugin. It adds the "classic" conversation style, where you can freely modify the syntax of the conversation.

## Why an add-on?

BetonQuest 1.7 introduced the conversation interface, which allows to quickly and esily create new conversation output types. Tellraw and inventory implementation of this interface required unified system of coloring the text. That's the reason why the old, prefix based conversation messages were removed. This add-on aims to bring it back for those who need it.

## Installation

To install this add-on just drop the Jar file into your _plugins_ directory and restart/reload your server.

## Usage

In the BetonQuest main _config.yml_ fie set `default_conversation_IO` option to "classic" and reload the plugin with _/q reload_ command.

By default the plugin is set to look exactly like the standard BetonQuest conversations do. Obviously you want to change that. Open the _config.yml_ file **from add-on's directory**, not from BetonQuest, and edit the options in the way you want them:

* **npc-prefix** - that's the prefix before the text said by the NPC. The `%quester%` keyword will be replaced by the NPC's name.
* **option-prefix** - the prefix before the option text, the one which the player will be able to choose. `%number%` will be replaced by the number of the option.
* **answer-prefix** - when the player chooses his answer, it will be printed using this prefix. `%player%` keyword will be replaced by his name.
