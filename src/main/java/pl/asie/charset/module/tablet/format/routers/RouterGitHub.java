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

import com.google.common.io.ByteStreams;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpResponse;
import pl.asie.charset.ModCharset;
import pl.asie.charset.module.tablet.TabletUtil;
import pl.asie.charset.module.tablet.format.api.IRouter;
import pl.asie.charset.module.tablet.format.parsers.GitHubMarkdownParser;
import pl.asie.charset.module.tablet.format.parsers.MarkdownParser;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class RouterGitHub implements IRouter {
	private final String host, hostIngame;

	public RouterGitHub(String host, String hostIngame) {
		this.host = host;
		this.hostIngame = hostIngame;
	}

	@Nullable
	@Override
	public String get(URI path) {
		String err = "";

		try {
			String pathCleaned = path.getPath();
			if (pathCleaned.length() <= 1) {
				pathCleaned = "/Home";
			}

			String hostPath = "https://raw.githubusercontent.com/wiki/" + host;

			URI uri = new URI(hostPath + pathCleaned + ".md");
			HttpClient client = TabletUtil.createHttpClient();
			HttpGet request = new HttpGet(uri);

			ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
			if (response.getCode() == 200) {
				MarkdownParser parser = new GitHubMarkdownParser(host, pathCleaned.substring(1));
				return parser.parse(new String(ByteStreams.toByteArray(response.getEntity().getContent()), StandardCharsets.UTF_8));
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
		return "wiki".equals(path.getScheme()) && hostIngame.equals(path.getHost());
	}
}
