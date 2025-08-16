/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.tablet.format.routers;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import pl.asie.charset.ModCharset;
import pl.asie.charset.module.tablet.TabletUtil;
import pl.asie.charset.module.tablet.format.api.IRouter;
import pl.asie.charset.module.tablet.format.parsers.MarkdownParser;

import javax.annotation.Nullable;
import java.net.URI;

public class RouterCoFH implements IRouter {
	private final String host = "https://raw.githubusercontent.com/CoFH/cofh.github.io/master";
	public RouterCoFH() {
	}

	@Nullable
	@Override
	public String get(URI path) {
		String err = "";

		try {
			String pathCleaned = path.getPath();
			if (pathCleaned.endsWith("/")) {
				pathCleaned = pathCleaned.substring(0, pathCleaned.length() - 1);
			}
			if (pathCleaned.endsWith("/index")) {
				pathCleaned = pathCleaned.substring(0, pathCleaned.length() - 6);
			}

			URI uri = new URI(host + pathCleaned + "/index.md");
			HttpClient client = TabletUtil.createHttpClient();
			HttpGet request = new HttpGet(uri);

			ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
			if (response.getCode() == 200) {
				MarkdownParser parser = new MarkdownParser();
				return parser.parse(new String(ByteStreams.toByteArray(response.getEntity().getContent()), Charsets.UTF_8));
			} else {
				err = err + "ERROR: " + response.getCode() + " " + response.getReasonPhrase() + "\n\n";
			}
		} catch (Exception e) {
			ModCharset.logger.error(e);
			return null;
		}

		return err.trim();
	}

	@Override
	public boolean matches(URI path) {
		return "wiki".equals(path.getScheme()) && "cofh".equals(path.getHost());
	}
}
