# ownCloud SMS Android Application Offical Repository

## Introduction

ownCloud SMS app push your Android devices conversation into your ownCloud instance, using ocsms app.

<a href="https://play.google.com/store/apps/details?id=fr.unix_experience.owncloud_sms">
  <img src="http://www.android.com/images/brand/android_app_on_play_large.png" alt="Download from Google Play" />
</a>
<a href="https://f-droid.org/repository/browse/?fdid=fr.unix_experience.owncloud_sms">
  <img src="https://camo.githubusercontent.com/7df0eafa4433fa4919a56f87c3d99cf81b68d01c/68747470733a2f2f662d64726f69642e6f72672f77696b692f696d616765732f632f63342f462d44726f69642d627574746f6e5f617661696c61626c652d6f6e2e706e67" alt="ownCloud Notes App on fdroid.org" />
</a>

ocsms app sources are available here: https://github.com/nerzhul/ocsms/

## Application documentation

You can found application documentation here: https://github.com/nerzhul/ownCloud-SMS-App/wiki

## Licence

ownCloud SMS Android Application licence is in reflexion, then sources are partial.

- App locales and layouts are under BSD 2 clause licence
- App DataProviders are under AGPLv3

## Contributions

We are searching for translations in others langs

To contribute please download `res/values/strings.xml` and `res/values/google_playstore_strings.xml` and give us a translated version!

You can also contribute by adding patches in Java code or cleanups.

## Requirements
- An ownCloud instance with ocsms app

## Build requirements
- nrz-androidlib (last version)
- ownCloud-Android-Library v1.0

## Issue template

Server
- ownCloud version: X.X.X
- PHP version: X.X
- HTTPd server: <apache|nginx...>
- HTTPS: <yes|no>

Client
- Android version: X.X.X
- Phone: <phone-model>
- ownCloud SMS app version: X.X.X

Please create your issues for the **client** here:

https://github.com/nerzhul/ownCloud-SMS-App/issues

And for the **server** app here:

https://github.com/nerzhul/ocsms/issues

## Developers

You can find our continuous integration here: http://jenkins.unix-experience.fr/job/ownCloud%20SMS%20%28Android%29/

### Coding guidelines

- No empty lines at EOF
- No trailing whitespaces
