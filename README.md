# Nextcloud SMS (Android)

[![codebeat badge](https://codebeat.co/badges/df05cef7-6724-4a2f-b170-96ed1ab793f6)](https://codebeat.co/projects/github-com-nerzhul-owncloud-sms-app-master)

Nextcloud SMS app pushes your Android devices conversation into your Nextcloud instance, using [ocsms app](https://github.com/nerzhul/ocsms).

## :arrow_forward: Access

[![Nextcloud Notes App on fdroid.org](https://camo.githubusercontent.com/7df0eafa4433fa4919a56f87c3d99cf81b68d01c/68747470733a2f2f662d64726f69642e6f72672f77696b692f696d616765732f632f63342f462d44726f69642d627574746f6e5f617661696c61626c652d6f6e2e706e67)](https://f-droid.org/repository/browse/?fdid=fr.unix_experience.owncloud_sms)

ocsms app sources are available here: https://github.com/nextcloud/ocsms

## :notebook: Application documentation

You can find the application documentation here: https://github.com/nerzhul/ncsms-android/wiki

## :link: Requirements
- [Nextcloud](https://nextcloud.com/) instance running
- [ocsms](https://github.com/nextcloud/ocsms) app enabled

## :exclamation: Reporting issues

- **Client:** https://github.com/nerzhul/ncsms-android/issues
- **Server:** https://github.com/nextcloud/ocsms/issues

## :rocket: Contributions

- We are searching for **translations** into others languages. To contribute please download `res/values/strings.xml` and `res/values/google_playstore_strings.xml` and provide a Pull Request with a translated version!
- You can also contribute by adding **patches** in Java code or cleanups.
- Application uses a [GoMobile AAR](https://gitlab.com/nerzhul/ncsmsgo) to have the best performance on phones with modern technologies like HTTP/2.0

### Build requirements
- gradle

### Coding guidelines

- No empty lines at EOF
- No trailing whitespaces

## :notebook: License

Nextcloud SMS Android Application license is in reflexion, then sources are partial.

- App locales and layouts are under BSD 2 clause license
- App DataProviders are under AGPLv3
