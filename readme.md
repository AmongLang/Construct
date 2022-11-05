**Construct**, not the java constructor, is a framework for data parsers and object factories. Construct provides abstraction layer
applicable from simple data to complex DSL, with convenient and effective error report system for end users and native integration
for [Among data expression format](https://github.com/AmongLang/Among).

```java
class WithoutConstruct{
    void readAndPrintInteger(Among among){
        if(among.isPrimitive()){
            try{
                System.out.println(among.asPrimitive().getIntValue());
            }catch(NumberFormatException ex){
                System.out.println("Expected int");
            }
        }else{
            System.out.println("Expected value");
        }
    }

    void readAndPrintUser(Among among){
        if(among.isObj()){
            AmongObject o = among.asObj();
            if(!o.hasProperty("name")){
                System.out.println("Missing property 'name'");
                return;
            }
            if(!o.hasProperty("age")){
                System.out.println("Missing property 'age'");
                return;
            }
            Among nameAmong = o.expectProperty("name");
            if(!nameAmong.isPrimitive()){
                System.out.println("Expected value for property 'name'");
                return;
            }
            Among ageAmong = o.expectProperty("age");
            if(!ageAmong.isPrimitive()){
                System.out.println("Expected value for property 'age'");
                return;
            }
            try{
                int age = ageAmong.asPrimitive().getIntValue();
                User user = new User(nameAmong.asPrimitive().getValue(), age);
                System.out.println(user);
            }catch(NumberFormatException ex){
                System.out.println("Expected int for property 'age'");
            }
        }else{
            System.out.println("Expected object");
        }
    }
}

class WithConstruct{
    void readAndPrintInteger(Among among){
        @Nullable Integer integer = Constructors.INT.construct(among, ReportHandler.simple());
        if(integer==null) return;
        System.out.println(integer);
    }

    Constructor<Among, User> USER_CONSTRUCTOR = Constructor.generifyObject(
            ConditionedConstructor.objectCondition(c -> c
                            .property("name", TypeFlags.PRIMITIVE)
                            .property("age", TypeFlags.PRIMITIVE)
                            .warnOtherProperties(),
                    (obj, reportHandler) -> {
                        @Nullable Integer age = Constructors.INT.construct(obj.expectProperty("age"), reportHandler);
                        if(age==null) return null;
                        return new User(obj.expectProperty("name").asPrimitive().getValue(), age);
                    }));

    void readAndPrintUser(Among among){
        @Nullable User user = USER_CONSTRUCTOR.construct(among, ReportHandler.simple());
        if(user==null) return;
        System.out.println(user);
    }
}
```

# Artifact

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.amonglang:construct:${among_version}"
}
```