package fr.unix_experience.owncloud_sms.enums;

/*
 * Copyright (c) 2014-2015, Loic Blot <loic.blot@unix-experience.fr>
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

public enum MailboxID {
	INBOX(0),
	SENT(1),
	DRAFTS(2),
	ALL(3);

	MailboxID(int id) {
		switch (id) {
			case 0: uri = "content://sms/inbox"; break;
			case 1: uri = "content://sms/sent"; break;
			case 2: uri = "content://sms/drafts"; break;
			case 3: uri = "content://sms"; break;
			default: throw new AssertionError();
		}
		this.id = id;
	}

	public static MailboxID fromInt(int id) {
		switch (id) {
			case 0: return MailboxID.INBOX;
			case 1: return MailboxID.SENT;
			case 2: return MailboxID.DRAFTS;
			case 3: return MailboxID.ALL;
			default: throw new AssertionError();
		}
	}
	private final String uri;
	private final int id;
	public int getId() { return id; }
	public String getURI() { return uri; }
}
