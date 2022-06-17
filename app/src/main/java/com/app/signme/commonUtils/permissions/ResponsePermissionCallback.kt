package com.app.signme.commonUtils.permissions

interface ResponsePermissionCallback {
    fun onResult(permissionResult: List<String>)
}