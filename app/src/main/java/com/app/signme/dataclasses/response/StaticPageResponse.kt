package com.app.signme.dataclasses.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class is used to hold Static Pages response
 * @property pageId String?
 * @property pageTitle String?
 * @property pageCode String?
 * @property content String?
 * @constructor
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class StaticPageResponse(
	@JsonProperty("page_id")
	val pageId: String? = null,

	@JsonProperty("page_title")
	val pageTitle: String? = null,

	@JsonProperty("page_code")
	val pageCode: String? = null,

	@JsonProperty("page_content")
	val content: String? = null
)