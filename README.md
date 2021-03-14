# Validation mapper

Powerful reflection-based data validator. Mostly used for mapping and validating network-layer models

## Install

Base library [ ![Download](https://maven-badges.herokuapp.com/maven-central/ru.ztrap.tools/validate-mapper/badge.svg) ](https://maven-badges.herokuapp.com/maven-central/ru.ztrap.tools/validate-mapper/)

```gradle
implementation "ru.ztrap.tools:validate-mapper:${latestVersion}"
```

Common checks [ ![Download](https://maven-badges.herokuapp.com/maven-central/ru.ztrap.tools/validate-mapper-common/badge.svg) ](https://maven-badges.herokuapp.com/maven-central/ru.ztrap.tools/validate-mapper-common/)

```gradle
implementation "ru.ztrap.tools:validate-mapper-common:${latestVersion}"
```

Gson extension [ ![Download](https://maven-badges.herokuapp.com/maven-central/ru.ztrap.tools/validate-mapper-gson/badge.svg) ](https://maven-badges.herokuapp.com/maven-central/ru.ztrap.tools/validate-mapper-gson/)

```gradle
implementation "ru.ztrap.tools:validate-mapper-gson:${latestVersion}"
```

## Usage

### 1. Basics

Let's imagine we have a simple `Message` model which contains `id`, `senderId`, `text` and `attachmentUrl`.
Fields `id` and `senderId` should not be empty or blank, `text` and `attachmentUrl` are not required and can be `null`. 
In code, it will be represented like this:

```kotlin
data class MessageNetworkEntity(
    @Checks(NotBlankStringCheck::class)
    val id: String?,
    @Checks(NotBlankStringCheck::class)
    val senderId: String?,
    @Checks(NotBlankStringCheck::class)
    val text: String?,
    @NotRequired
    val attachmentUrl: String?
) {
    companion object : ValidateMapper<MessageNetworkEntity, MessageDomainEntity>() {
        override fun transform(raw: MessageNetworkEntity) = MessageDomainEntity(
            id = requireNotNull(raw.id),
            text = raw.text,
            senderId = requireNotNull(raw.senderId),
            attachmentUrl = raw.attachmentUrl
        )
    }
}

data class MessageDomainEntity(
    val id: String,
    val text: String,
    val senderId: String,
    val attachmentUrl: String?
)
```

We can validate this network model with:

```kotlin
val networkMessage = MessageNetworkEntity(
    id = "7c0518f7-874d-4dd7-89b6-af0adf0699a1",
    senderId = "347da584-9f8c-4b47-b912-f9f2af0a6417",
    text = "Hello!",
    attachmentUrl = "https://avatars.githubusercontent.com/u/18091396?s=460&u=72fb5fb2f4d8a2ef3b6195ee371a130532656095&v=4"
)

val domainMessage = networkMessage.validateMap(MessageNetworkEntity)
```

If some constraint is not met, `validateMap` will throw a `FailedValidationException` and provide detailed explanation of what is not met.
Example:

```kotlin
val networkMessage = MessageNetworkEntity(
    id = "",
    senderId = " ",
    text = null,
    attachmentUrl = "https://avatars.githubusercontent.com/u/18091396?s=460&u=72fb5fb2f4d8a2ef3b6195ee371a130532656095&v=4"
)

val domainMessage = networkMessage.validateMap(MessageNetworkEntity)
```

Will throw exception with following message:
```
ru.ztrap.tools.validate.mapper.FailedValidationException: Failed validation of received object.
    Object -> MessageNetworkEntity(id=, senderId= , text=null, attachmentUrl=https://avatars.githubusercontent.com/u/18091396?s=460&u=72fb5fb2f4d8a2ef3b6195ee371a130532656095&v=4)
    Params -> id ------- Reasons -> [{reason="empty string"}],
              senderId - Reasons -> [{reason="blank string"}],
              text ----- Reasons -> [{reason="value is null"}]
    ...
```

### 2. Parametrized checks

You can provide named parameters into any `ValidateChecker` via any of this annotations

```kotlin
annotation class StringParameter(val name: String, val value: String)
annotation class ByteParameter(val name: String, val value: Byte)
annotation class ShortParameter(val name: String, val value: Short)
annotation class IntParameter(val name: String, val value: Int)
annotation class LongParameter(val name: String, val value: Long)
annotation class FloatParameter(val name: String, val value: Float)
annotation class DoubleParameter(val name: String, val value: Double)
annotation class KClassParameter(val name: String, val value: KClass<*>)
```

Let's add length checks to unique id fields:

```kotlin
private const val MAX_UUID_LENGTH = 36L

data class MessageNetworkEntity(
    @CheckParametrized(
        expressionClass = TrimmedStringLengthCheck::class,
        long = [
            CheckParametrized.LongParameter(TrimmedStringLengthCheck.MAX_LIMIT_LONG, MAX_UUID_LENGTH),
            CheckParametrized.LongParameter(TrimmedStringLengthCheck.MIN_LIMIT_LONG, MAX_UUID_LENGTH)
        ]
    )
    val id: String?,
    @CheckParametrized(
        expressionClass = TrimmedStringLengthCheck::class,
        long = [
            CheckParametrized.LongParameter(TrimmedStringLengthCheck.MAX_LIMIT_LONG, MAX_UUID_LENGTH),
            CheckParametrized.LongParameter(TrimmedStringLengthCheck.MIN_LIMIT_LONG, MAX_UUID_LENGTH)
        ]
    )
    val senderId: String?,
    ...
)
```

And validate:

```kotlin
val networkMessage = MessageNetworkEntity(
    id = "52b1c9c5-3cb2-41f0", // invalid
    senderId = "52b1c9c5-3cb2-41f0-adff-1b0da5cffeee",
    text = null,
    attachmentUrl = "https://avatars.githubusercontent.com/u/18091396?s=460&u=72fb5fb2f4d8a2ef3b6195ee371a130532656095&v=4"
)

val domainMessage = networkMessage.validateMap(MessageNetworkEntity)
```

As result, we get thrown exception with following message:

```
ru.ztrap.tools.validate.mapper.FailedValidationException: Failed validation of received object.
    Object -> MessageNetworkEntity(id=52b1c9c5-3cb2-41f0, senderId=52b1c9c5-3cb2-41f0-adff-1b0da5cffeee, text=null, attachmentUrl=https://avatars.githubusercontent.com/u/18091396?s=460&u=72fb5fb2f4d8a2ef3b6195ee371a130532656095&v=4)
    Params -> id - Reasons -> [{reason="length not in limits", values=[min_limit=36, max_limit=36, current_length=18]}]
    ...
```

### 3. Global configuration

You can define checks for all fields with provided type. For example, we expect all strings can't be blank. 

```kotlin
ValidateMapper.addGlobalCheck(String::class, NotBlankStringCheck::class)
```

In some cases you may want to exclude checks from global pool for some field. You can simply do this with:

```kotlin
data class MessageNetworkEntity(
    @ExcludeCkecks(NotBlankStringCheck::class)
    val text: String?,
    ...
)
```

Also, you can provide field name extractor. Actual for models parsed from json. 
Example you can find [here](https://github.com/zTrap/validate-mapper/blob/master/validate-mapper-gson/src/main/kotlin/ru/ztrap/tools/validate/gson/ValidateExtensionGson.kt)

## Developed By

- Peter Gulko
- ztrap.developer@gmail.com
- [paypal.me/zTrap](https://www.paypal.me/zTrap)

## License

       Copyright 2021 zTrap

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
