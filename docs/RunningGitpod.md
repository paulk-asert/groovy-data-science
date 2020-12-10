## Running via Gitpod

[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/paulk-asert/groovy-constraint-programming)

To run via gitpod. Click on the Gitpod button on the github site.
Once loaded, run one of the available script tasks. To find
available script tasks you can try something like:

```
> ./gradlew :HousePricesBeam:tasks --group="Scripts"
```

You should see something like below:
![Gitpod tasks](images/Gitpod.png)

And you can run a script with something like:
```
> ./gradlew :HousePricesBeam:runHousePricesBeam
```

With the following result:
![Gitpod result](images/GitpodResult.png)

__Requirements__: Gitpod isn't currently set up for scripts which
use GroovyFX or display plots using a browser. It would try to
display the output in the cloud rather than via your local machine.