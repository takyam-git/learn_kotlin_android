package com.takyam.push.mr.mrpush

import java.text.SimpleDateFormat
import java.util.*

class Novel(title: String, number: Int?, updateTime: Date?) : Comparable<Novel> {
    val title: String = title
    val number: Int? = number
    val updateTime: Date? = updateTime
    val key: String get() = "${title}:${number}"
    override fun toString(): String {
        var str = title
        if (number != null) {
            str += " ${number.toString()}話"
        }
        if (updateTime != null) {
            str += " ${SimpleDateFormat("MM/dd HH:mm").format(updateTime)}"
        }
        return str
    }

    override fun equals(other: Any?): Boolean {
        return other is Novel && other.key == this.key
    }

    override fun compareTo(other: Novel): Int {
        if (this.key == other.key) {
            return 0
        }
        if (this.updateTime != null && other.updateTime != null) {
            return this.updateTime.compareTo(other.updateTime)
        } else {
            return -1
        }
    }
}