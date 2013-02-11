#!/bin/sh

# DAFFODIL_CLASSPATH
#
# The Daffodil implementation will look on the classpath for 'includes' and
# 'imports'. To defined additional directories where Daffodil should look for
# files, set the DAFFODIL_CLASSPATH environment variable, for example:
#
#   export DAFFODIL_CLASSPATH="/path/to/imports/:/path/to/includes/"
#
# In addition to defining directories to search for imports and includes, you
# can add a "CatalogManager.properties" file to the DAFFODIL_CLASSPATH to
# direct Daffodil to a relative path location of a user XML Catalog.
#
#
# DAFFODIL_JAVA_OPTS
#
# If you need to specify java options specific to Daffodil, you can set the
# DAFFODIL_JAVA_OPTS environment variable. If not specified, the JAVA_OPTS
# environment variable will be used. If that is not specified, reasonable
# defaults for Daffodil will be defined.


MAINCLASS=daffodil.Main
LIBDIR="../lib"
CLASSPATH=$(find $LIBDIR -name '*.jar' | xargs echo | tr ' ' ':')

JOPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m -XX:ReservedCodeCacheSize=128m"

if [ -n "$DAFFODIL_JAVA_OPTS" ]; then
	JOPTS="$DAFFODIL_JAVA_OPTS"
elif [ -n "$JAVA_OPTS" ]; then
	JOPTS="$JAVA_OPTS"
fi

exec java $JOPTS -cp "$CLASSPATH:$DAFFODIL_CLASSPATH" "$MAINCLASS" "$@"