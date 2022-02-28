Locale Plugin for Jenkins
=========================


[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/locale.svg)](https://plugins.jenkins.io/locale)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/locale-plugin.svg?label=changelog)](https://github.com/jenkinsci/locale-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/locale.svg?color=blue)](https://plugins.jenkins.io/locale)

This plugin controls the language of Jenkins.

Normally, Jenkins honors the browser's language preference if a translation is available for the preferred language,
and uses the system default locale for messages during a build.
This plugin allows you to:

* override the system default locale to the language of your choice
* ignore browser's language preference completely

This feature is sometimes convenient for multi-lingual environment.

### Usage
Under _Manage Jenkins > Configure System_ there should be a "Locale" section.

Here you can enter the _Default Language_: this should be a language code
or locale code like "fr" (for French), or "de_AT" (German, in Austria).

This value will be used by the system, for example, for messages that are printed
to the log during a build (assuming that the Jenkins features and plugins that
you're using have been translated into the specified language).

To additionally force this language on all users, overriding their browser language,
you can check the "Ignore browser preference and force this language to all users" option.

JCasC is supported on all version after 1.4

```
unclassified:
  locale:
    systemLocale: en
    ignoreAcceptLanguage: true
```

### Changelog

* See [GitHub releases](https://github.com/jenkinsci/locale-plugin/releases) for new releases
* For versions 1.4 and older, see the [changelog archive](docs/CHANGELOG.old.md)
