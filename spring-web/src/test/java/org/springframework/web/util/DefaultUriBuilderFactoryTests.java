/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for {@link DefaultUriBuilderFactory}.
 * @author Rossen Stoyanchev
 */
public class DefaultUriBuilderFactoryTests {

	@Test
	public void defaultSettings() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		URI uri = factory.uriString("/foo").pathSegment("{id}").build("a/b");
		assertEquals("/foo/a%2Fb", uri.toString());
	}

	@Test
	public void baseUri() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("http://foo.com/bar?id=123");
		URI uri = factory.uriString("/baz").port(8080).build();
		assertEquals("http://foo.com:8080/bar/baz?id=123", uri.toString());
	}

	@Test
	public void baseUriWithPathOverride() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("http://foo.com/bar");
		URI uri = factory.uriString("").replacePath("/baz").build();
		assertEquals("http://foo.com/baz", uri.toString());
	}

	@Test
	public void defaultUriVars() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("http://{host}/bar");
		factory.setDefaultUriVariables(singletonMap("host", "foo.com"));
		URI uri = factory.uriString("/{id}").build(singletonMap("id", "123"));
		assertEquals("http://foo.com/bar/123", uri.toString());
	}

	@Test
	public void defaultUriVarsWithOverride() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("http://{host}/bar");
		factory.setDefaultUriVariables(singletonMap("host", "spring.io"));
		URI uri = factory.uriString("/baz").build(singletonMap("host", "docs.spring.io"));
		assertEquals("http://docs.spring.io/bar/baz", uri.toString());
	}

	@Test
	public void defaultUriVarsWithEmptyVarArg() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("http://{host}/bar");
		factory.setDefaultUriVariables(singletonMap("host", "foo.com"));
		URI uri = factory.uriString("/baz").build();
		assertEquals("Expected delegation to build(Map) method", "http://foo.com/bar/baz", uri.toString());
	}

	@Test
	public void defaultUriVarsSpr14147() throws Exception {
		Map<String, String> defaultUriVars = new HashMap<>(2);
		defaultUriVars.put("host", "api.example.com");
		defaultUriVars.put("port", "443");
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		factory.setDefaultUriVariables(defaultUriVars);

		URI uri = factory.expand("https://{host}:{port}/v42/customers/{id}", singletonMap("id", 123L));
		assertEquals("https://api.example.com:443/v42/customers/123", uri.toString());
	}

	@Test
	public void encodingValuesOnly() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		factory.setEncodingMode(EncodingMode.VALUES_ONLY);
		UriBuilder uriBuilder = factory.uriString("/foo/a%2Fb/{id}");

		String id = "c/d";
		String expected = "/foo/a%2Fb/c%2Fd";

		assertEquals(expected, uriBuilder.build(id).toString());
		assertEquals(expected, uriBuilder.build(singletonMap("id", id)).toString());
	}

	@Test
	public void encodingValuesOnlySpr14147() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		factory.setEncodingMode(EncodingMode.VALUES_ONLY);
		factory.setDefaultUriVariables(singletonMap("host", "www.example.com"));
		UriBuilder uriBuilder = factory.uriString("http://{host}/user/{userId}/dashboard");

		assertEquals("http://www.example.com/user/john%3Bdoe/dashboard",
				uriBuilder.build(singletonMap("userId", "john;doe")).toString());
	}

	@Test
	public void encodingNone() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		factory.setEncodingMode(EncodingMode.NONE);
		UriBuilder uriBuilder = factory.uriString("/foo/a%2Fb/{id}");

		String id = "c%2Fd";
		String expected = "/foo/a%2Fb/c%2Fd";

		assertEquals(expected, uriBuilder.build(id).toString());
		assertEquals(expected, uriBuilder.build(singletonMap("id", id)).toString());
	}

	@Test
	public void initialPathSplitIntoPathSegments() throws Exception {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("/foo/{bar}");
		URI uri = factory.uriString("/baz/{id}").build("a/b", "c/d");
		assertEquals("/foo/a%2Fb/baz/c%2Fd", uri.toString());
	}

}