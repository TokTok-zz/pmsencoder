#!/usr/bin/env perl

# rtmp2pms -u: convert rtmpdump command lines to PMSEncoder rtmpdump:// URIs
# rtmp2pms -p: convert simple text files with stream names, (optional) thumbnail URLs
#              and rtmpdump commands to PMS WEB.conf lines (see news.txt)
#
# usage:
#
#     cat news.txt | rtmp2pms -p
#
# or:
#
#     rtmp2pms -p news.txt ...
#
# or:
#
#     cat command_lines.txt | rtmp2pms -u
#
# or:
#
#     rtmp2pms -u file1 file2 ...

use strict;
use warnings;

use Getopt::Long qw(:config posix_default no_ignore_case bundling);
use URI::Escape qw(uri_escape);

our ($PMS, $URI);
our $FOLDER = 'Web,Live News';

{
    my ($name, $thumb);

    sub pms($) {
        my $line = shift;
        return unless ($line =~ /^(name|command|thumb):\s*(.+$)/);

        if ($1 eq 'name') {
            $name = $2;
        } elsif ($1 eq 'thumb') {
            $thumb = $2;
        } else {
            my $uri = uri($2);
            $thumb = $thumb ? ",$thumb" : '';
            print "videostream.$FOLDER=$name,$uri$thumb", $/;
        }
    }
}

sub uri($) {
    my $command = shift;
    return unless ($command =~ s{^.*?rtmpdump(?:\.exe)?\s+}{}i);
    $command =~ s{\s+$}{};
    $command =~ s{["']}{}g;
    $command =~ s{(--?\w+)\s+(?!-)(\S+)}{"$1=" . uri_escape($2)}eg;
    $command =~ s{\s+}{&}g;
    return "rtmpdump://rtmp2pmspl?$command";
}

GetOptions(
    'pms|p'      => \$PMS,
    'uri|u'      => \$URI,
    'folder|f=s' => \$FOLDER,
);

while (<>) {
    chomp;
    if ($PMS) {
        pms($_);
    } elsif ($URI) {
        print uri($_), $/;
    }
}
