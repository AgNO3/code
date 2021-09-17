# coding=utf-8
import simplejson
import sys
import codecs

sys.stdout = codecs.getwriter('utf8')(sys.stdout)

qwerty = r'''
`~ 1! 2@ 3# 4$ 5% 6^ 7& 8* 9( 0) -_ =+
    qQ wW eE rR tT yY uU iI oO pP [{ ]} \|
     aA sS dD fF gG hH jJ kK lL ;: '"
      zZ xX cC vV bB nN mM ,< .> /?
'''


de = ur'''
^° 1! 2" 3§ 4$ 5% 6& 7/ 8( 9) 0= ß? ´`
    qQ wW eE rR tT zZ uU iI oO pP üÜ +* 
     aA sS dD fF gG hH jJ kK lL öÖ äÄ #' 
   <> yY xX cC vV bB nN mM ,; .: -_ 
'''

dvorak = r'''
`~ 1! 2@ 3# 4$ 5% 6^ 7& 8* 9( 0) [{ ]}
    '" ,< .> pP yY fF gG cC rR lL /? =+ \|
     aA oO eE uU iI dD hH tT nN sS -_
      ;: qQ jJ kK xX bB mM wW vV zZ
'''

jis = ur'''
1! 2" 3# 4$ 5% 6& 7' 8( 9) 00 -= ^~ ¥|
 qQ wW eE rR tT yY uU iI oO pP @` [{
  aA sS dD fF gG hH jJ kK lL ;+ :* ]}
   zZ xX cC vV bB nN mM ,< .> /?
'''

keypad = r'''
  / * -
7 8 9 +
4 5 6
1 2 3
  0 .
'''

mac_keypad = r'''
  = / *
7 8 9 -
4 5 6 +
1 2 3
  0 .
'''

layouts = [('qwerty', (qwerty, True)),
        ('de', (de, True)),
        ('jis', (jis, True)),
        ('dvorak', (dvorak, True)),
        ('keypad', (keypad, False)),
        ('mac_keypad', (mac_keypad, False))]

def get_slanted_adjacent_coords(x, y):
    '''
    returns the six adjacent coordinates on a standard keyboard, where each row is slanted to the right from the last.
    adjacencies are clockwise, starting with key to the left, then two keys above, then right key, then two keys below.
    (that is, only near-diagonal keys are adjacent, so g's coordinate is adjacent to those of t,y,b,v, but not those of r,u,n,c.)
    '''
    return [(x-1, y), (x, y-1), (x+1, y-1), (x+1, y), (x, y+1), (x-1, y+1)]

def get_aligned_adjacent_coords(x, y):
    '''
    returns the nine clockwise adjacent coordinates on a keypad, where each row is vertically aligned.
    '''
    return [(x-1, y), (x-1, y-1), (x, y-1), (x+1, y-1), (x+1, y), (x+1, y+1), (x, y+1), (x-1, y+1)]

def build_graph(layout_str, slanted):
    '''
    builds an adjacency graph as a dictionary: {character: [adjacent_characters]}.
    adjacent characters occur in a clockwise order.
    for example:
    * on qwerty layout, 'g' maps to ['fF', 'tT', 'yY', 'hH', 'bB', 'vV']
    * on keypad layout, '7' maps to [None, None, None, '=', '8', '5', '4', None]
    '''
    position_table = {} # maps from tuple (x,y) -> characters at that position.
    tokens = layout_str.split()
    token_size = len(tokens[0])
    x_unit = token_size + 1 # x position unit length is token length plus 1 for the following whitespace.
    adjacency_func = get_slanted_adjacent_coords if slanted else get_aligned_adjacent_coords

    assert all(len(token) == token_size for token in tokens), 'token length mismatch:\n ' + layout_str
    for y, line in enumerate(layout_str.split('\n')):
        slant = y - 1 if slanted else 0 # the way i illustrated keys above, each qwerty row is indented one space in from the last
        for token in line.split():
            x, remainder = divmod(line.index(token) - slant, x_unit)
            assert remainder == 0, 'unexpected x offset for %s in:\n%s' % (token, layout_str)
            position_table[(x,y)] = token

    adjacency_graph = {}
    for (x,y), chars in position_table.iteritems():
        for char in chars:
            adjacency_graph[char] = []
            for coord in adjacency_func(x, y):
                # position in the list indicates direction (for qwerty, 0 is left, 1 is top, 2 is top right, ...)
                # for edge chars like 1 or m, insert None as a placeholder when needed so that each character in the graph has a same-length adjacency list.
                adjacency_graph[char].append(position_table.get(coord, None))
    return adjacency_graph

def escape(c):
    if c == "\\" or c == "'":
         c = "\\" + c
    return c

if __name__ == '__main__':

    print "package eu.agno3.runtime.security.password.internal.generated;"
    print "import java.util.HashMap;"
    print "import java.util.Map;"
    print "import javax.annotation.Generated;"
    print "@Generated ( \"zxcvbn generator\" )"
    print "@SuppressWarnings ( \"javadoc\" )"
    print "public final class AdjacencyGraphs {"

    print  "    public static final Map<String, Map<Character,char[][]>> ALL_KEYMAPS = new HashMap<>();"
    for graph_name, args in layouts:
        print "     public static final Map<Character,char[][]> %s = new HashMap<>();" % (graph_name.upper())

    print
    print "     private AdjacencyGraphs() {}"
    print
    print "     static {"
    for graph_name, args in layouts:

        graph = build_graph(*args)

        for c, adjacents in graph.items():
            c = escape(c)
            print "        %s.put('%s', new char[][] {" % (graph_name.upper(), c)
            for adjacent in adjacents:
                if not adjacent:
                    print "           null,"
                elif len(adjacent) == 2:
                    print "           new char[] { '%s', '%s' }," % (escape(adjacent[0]), escape(adjacent[1]))
                else:
                    print "           new char[] { '%s', 0 }," % (escape(adjacent[0]))
            print "        });"

        print "        ALL_KEYMAPS.put(\"%s\", %s); //$NON-NLS-1$" % (graph_name, graph_name.upper());

    print "     }"
    print "}"

