package com.app.signme.core

/**
 * This class is used to show settings on setting screen
 * @property showNotification Boolean
 * @property showRemoveAdd Boolean
 * @property showEditProfile Boolean
 * @property showChangePhone Boolean
 * @property showChangePassword Boolean
 * @property showDeleteAccount Boolean
 * @property showSendFeedback Boolean
 * @property showLogOut Boolean
 * @property appVersion String
 */
class SettingViewConfig {
    var showNotification: Boolean = true
    var showRemoveAdd: Boolean = true
    var showRemoveSubscription: Boolean = true
    var showEditProfile: Boolean = true
    var showChangePhone: Boolean = true
    var showChangePassword: Boolean = true
    var showDeleteAccount: Boolean = true
    var showSendFeedback: Boolean = true
    var showLogOut: Boolean = true
    var appVersion:String = ""

   /* *//**
     * If there is no any visible setting for Account setting, then hide Account setting Option
     *//*
    fun showAccountHeaderShow():Boolean{
        return (!showNotification &&
                !showRemoveAdd &&
                !showRemoveSubscription &&
                !showChangePassword &&
                !showChangePhone &&
                !showDeleteAccount &&
                !showEditProfile)
    }*/

}