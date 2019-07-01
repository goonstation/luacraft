# Paths
JDK_DIR=/run/media/jake/storage/Developer/Java/jdk1.8.0_211
LUA_INC_DIR=/home/jake/Developer/Jitjat/src
VERSION=jit
ARCH=amd64
#ARCH=i386

# Tools
MD=mkdir
CC=gcc
LD=gcc

# Default arguments
CFLAGS=-c -fno-strict-aliasing -fPIC -O2 -Wall -DNDEBUG -D_REENTRANT -DLUA_USE_LINUX -DLUA_USE_POSIX 
LDFLAGS=-shared

# Description blocks
all: 32bit 64bit

32bit:
	$(MD) -p 32
	$(CC) $(CFLAGS) -m32 -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux -I$(LUA_INC_DIR) -o 32/jnlua.o ../jnlua.c
	$(LD) $(LDFLAGS) -m32 -o 32/libjnlua$(VERSION).so -L$(LUA_INC_DIR) 32/jnlua.o -lc -llua$(VERSION)

64bit:
	$(MD) -p 64
	$(CC) $(CFLAGS) -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux -I$(LUA_INC_DIR) -o 64/jnlua.o ../jnlua.c
	$(LD) $(LDFLAGS) -o 64/libjnlua$(VERSION).so -L$(LUA_INC_DIR) 64/jnlua.o -lc -llua$(VERSION)

clean:
	-rm 32/libjnlua$(VERSION).so 32/jnlua.o
	-rm 64/libjnlua$(VERSION).so 64/jnlua.o