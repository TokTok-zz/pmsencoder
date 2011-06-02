var command = prompt(
    'Please enter the rtmpdump command line',
    'rtmpdump -v -r rtmp://example.org -s http://example.org -p http://example.org'
);

if (command) {
    command = command.replace(/^.*?rtmpdump(?:\.exe)?\s+/i, '').
        replace(/\s+$/, '').
        replace(/["']/g, '').
        replace(/(--?\w+)\s+(?!-)(\S+)/g, function(match, key, value) { return key + '=' + escape(value) }).
        replace(/\s+/g, '&');
    document.write('rtmpdump://rtmp2pmsjs?' + command);
}
