import os
fp = open('./Letter to the Templars — Part 3/a.txt')
key = 7326154
from textwrap import wrap
words = wrap(fp.read(), 7)
# print(words)
rev_key = '5367241'
new_words = []
for word in words:
    new_word = ['*']*7
    i=0
    for k in rev_key:
        ik = int(k)
        new_word[ik-1] = word[i]
        i += 1
    new_words.append(''.join(new_word))
print(new_words)
