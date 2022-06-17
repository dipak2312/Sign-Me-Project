package com.app.signme.dataclasses

import android.net.Uri

/**
 * This class is used to hold feedback images
 * @property contentUri uri of image
 * @property imagePath String path of image
 * @constructor
 */
class FeedbackImageModel (var contentUri: Uri? = null,
                          var imagePath: String = ""){

  /*  *
     * This will show add image button in list item for add image
     */
    fun showAddButton():Boolean{
        return contentUri == null && return imagePath == ""
    }



}