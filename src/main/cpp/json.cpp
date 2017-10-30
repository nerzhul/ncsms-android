/**
 *  Copyright (c) 2017, Loic Blot <loic.blot@unix-experience.fr>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include "json.h"
#include <iomanip>
#include <sstream>

namespace json {

std::string escape_string(const char *str)
{
	if (!str) {
		return "";
	}

	std::string result;
	// Create a sufficient buffer to escape all chars
	result.reserve(strlen(str) * 2 + 3);
	result += "\"";
	for (const char *c = str; *c != 0; ++c) {
		switch (*c) {
			case '\"':
				result += "\\\"";
				break;
			case '\\':
				result += "\\\\";
				break;
			case '\b':
				result += "\\b";
				break;
			case '\t':
				result += "\\t";
				break;
			case '\n':
				result += "\\n";
				break;
			case '\f':
				result += "\\f";
				break;
			case '\r':
				result += "\\r";
				break;
			default:
				if (is_control_character(*c)) {
					std::stringstream oss;
					oss << "\\u" << std::hex << std::uppercase << std::setfill('0')
						<< std::setw(4) << static_cast<int>(*c);
					result += oss.str();
				} else {
					result += *c;
				}
				break;
		}
	}
	result += "\"";
	return result;
}
}