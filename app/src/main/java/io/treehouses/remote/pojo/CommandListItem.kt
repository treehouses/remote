package io.treehouses.remote.pojo

class CommandListItem {
    var title: String? = null
    var command: String? = null

    constructor(title: String?, command: String?) {
        this.title = title
        this.command = command
    }

    constructor() {}

}