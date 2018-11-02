/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junitpioneer.gradle

import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.service.ResolveService
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class TriggerTravisTask extends DefaultTask {

	private static final Headers TRAVIS_API_VERSION = Headers.of "Travis-API-Version": "3"
	private static final MediaType JSON = MediaType.parse "application/json; charset=utf-8"
	private static final int OK = 200
	private static final int ACCEPTED = 202
	private static final Set<Integer> OK_CODES = [ OK, ACCEPTED ]

	private final OkHttpClient client = new OkHttpClient()

	String travisProject
	String branch = "master"
	String apiToken
	String message = "Triggering build of %PROJECT"

	@TaskAction
	def trigger() {
		checkConfiguration()
		def message = createMessage()
		println message
		def response = postToTravis(travisProject, branch, message)
		if (OK_CODES.contains(response.code()))
			logger.lifecycle("Successfully triggered Travis build. $response ")
		else
			throw new GradleException("Triggering Travis build failed. $response ")
	}

	private void checkConfiguration() {
		boolean configurationInvalid = false;
		if (travisProject == null || travisProject.allWhitespace) {
			configurationInvalid = true;
			logger.error "For the task '${getName()}', no Travis project name has been defined."
		}
		if (apiToken == null || apiToken.allWhitespace) {
			configurationInvalid = true;
			logger.error "For the task '${getName()}', no API token has been defined."
		}
		if (configurationInvalid)
			throw new GradleException("To trigger a Travis build, please define both a project name and an API token.")
	}

	String createMessage() {
		def msg = message.replaceAll("%PROJECT", travisProject)
		if (message.contains("%COMMIT"))
			msg = msg.replaceAll("%COMMIT", determineCommit())
		return msg
	}

	String determineCommit() {
		def repo = Grgit.open().repository
		return new ResolveService(repo).toCommit("HEAD").getAbbreviatedId()
	}

	Response postToTravis(String project, String branch, String message) throws IOException {
		def url = "https://api.travis-ci.org/repo/${project.replaceAll("/", "%2F")}/requests"
		def requestBody = """{ "request": { "message": "$message", "branch": "$branch" } }"""

		def request = new Request.Builder()
				.url(url)
				.headers(TRAVIS_API_VERSION)
				.header("Authorization", "token $apiToken")
				.post(RequestBody.create(JSON, requestBody))
				.build()
		return client.newCall(request).execute()
	}

}
