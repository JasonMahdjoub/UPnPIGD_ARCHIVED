# Security Policy

## Supported Versions

| Version | Supported          |
|---------| ------------------ |
| >=1.0.2 | :heavy_check_mark: |
| <1.0.2  | :x:                |

## Reporting a Vulnerability

To report a vulnerability, please report security issues related to the project to the
following email address:

    jason.mahdjoub@distri-mind.fr. 

## Verifying contents

All commits are signed. All artifacts published on [artifactory.distri-mind.fr](https://artifactory.distri-mind.fr) and on Maven central are signed. For
each artifact, there is an associated signature file with the .asc
suffix.

### After 2023-09-12

To verify the signature use [this public key](key-2023-10-09.pub). Here is its fingerprint:
```
pub   rsa4096/2CDFE2681CA78FB0 2023-10-09 [SC]
    fingerprint = DF24 38C0 BA4E 54BE 44A7  08F8 2CDF E268 1CA7 8FB0
uid   Jason Mahdjoub <jason.mahdjoub@distri-mind.fr>
sub   rsa4096/418D774BEDA1D018 2023-10-09 [E]
```

A copy of this key is stored on the
[keyserver.ubuntu.com](https://keyserver.ubuntu.com/) keyserver. To add it to
your public key ring use the following command:

```
> gpg  --keyserver hkps://keyserver.ubuntu.com --recv-keys DF2438C0BA4E54BE44A708F82CDFE2681CA78FB0
```
### Before 2023-09-12

To verify the signature use [this public key](key-2021-09-12.pub). Here is its fingerprint:
```
pub   rsa3072/9C2DA6BA1B635E5C 2021-09-12 [SC] [expired: 2023-09-12]
    fingerprint = 6246 711F F66C 2141 8537  3D8E 9C2D A6BA 1B63 5E5C
uid   Jason Mahdjoub <jason.mahdjoub@distri-mind.fr>
sub   rsa3072/D291AE792E148FCB 2021-09-12 [E] [expired: 2023-09-12]

```

A copy of this key is stored on the
[keyserver.ubuntu.com](https://keyserver.ubuntu.com/) keyserver. To add it to
your public key ring use the following command:

```
> gpg  --keyserver hkps://keyserver.ubuntu.com --recv-keys 6246711FF66C214185373D8E9C2DA6BA1B635E5C
```

## Preventing commit history overwrite

In order to prevent loss of commit history, developers of the project
are highly encouraged to deny branch deletions or history overwrites
by invoking the following two commands on their local copy of the
repository.


```
git config receive.denyDelete true
git config receive.denyNonFastForwards true
```