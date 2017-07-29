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

#pragma once

#define DECL_JNIMETHOD(name, type) \
    {#name, type, (void*) &name },

#define DECL_METHODSIZE(T) \
    int T::methods_size = sizeof(T::methods) / sizeof(T::methods[0]);

#define DECL_JNICLASSATTRS \
    static const char *classPathName; \
    static JNINativeMethod methods[]; \
    static int methods_size;