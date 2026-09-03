# competitivecounting

Counting bot that aims to be competitive and encourage thinking

help (coming soon): https://hyperlexus.uk/help/compcount/

bot setup (for documentation):
- from root, make `./src/data/` and put `config.txt`, `captures.json`[sic!] and `counters.json` in it
<br><br>
- put the bot token with no "" or () or TOKEN= or similar, just blank token into config.txt
- make some captchas in captures.json like so:
  - `{"captures": [ {"question":"test_captcha_answer_is_1","answer":1} ]}`
- counters.json is filled automatically.
<br><br>
- the bot requires gradle 7.4 and java 22 to work and should not struggle with packages, works out of the box
- invite using `https:/­/discord.com/oauth2/authorize?client_id= <client_id> &permissions=8&scope=bot`, where
the client id comes from https:/­/discord.com/developers/applications/<client_id> (your app you created)

build the bot using `./gradlew clean fatJar && java -jar app/build/libs/competitivecounting.jar` (for me* on linux)

\* me = not me