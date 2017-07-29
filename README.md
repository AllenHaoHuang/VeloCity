# VéloCity
**vélo [*noun, french*]**

    bike, cycle

**city [*noun*]**

    a large town.
 
The bicycle lies as one of the world's most important inventions. 
We aim to provide a bounding connection between a city, and its bicycles. 
Humanity deserves that - does it not?

### API
The optimal route-calculation API is hosted at http://api.velocity.shen.nz:1234. The API is hosted at [this repository](https://github.com/williamshen-nz/VeloCity-API).

To query the API, simply request (GET): `/getRoute.json?origin=...&destination=...&option=...` where option = Safest, Shortest, Fastest, Scenic or DEBUG (case-sensitive). DEBUG returns all potential routes and is, as its name suggests, used for debugging purposes.

Please refrain from using spaces in the origin and destination parameters. Spaces should be replaced with the "+" character.

Example of a well-formed query: http://api.velocity.shen.nz:1234/getRoute.json?origin=Lena+Karmel+Canberra&destination=Coles+Manuka&option=Scenic

### Sample Images
<img src="http://i.imgur.com/zJUqzfS.png" width="300px">

<img src="http://i.imgur.com/cakMCFr.png" width="300px">
