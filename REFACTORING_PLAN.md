# RT4 Package Refactoring Plan (Alphabetical)

## Instructions for IntelliJ IDEA

1. **Create all target packages first** (right-click on rt4 → New → Package)
2. **For each group below**: Select the files → Right-click → Refactor → Move → Enter package name
3. **IntelliJ will automatically update all imports**
4. **Files are listed alphabetically for easy selection**

---

## 1. rt4.audio (Audio System) - 32 files

**Package:** `rt4.audio`

```
AreaSound.java
AreaSoundManager.java
AudioChannel.java
AudioThread.java
JavaAudioChannel.java
Midi_Class162.java
MidiDecoder.java
MidiInstrument.java
MidiNote.java
MidiNoteStream.java
MidiPcmStream.java
MidiPlayer.java
MixerListener.java
MixerPcmStream.java
MusicPlayer.java
PcmResampler.java
PcmSound.java
PcmStream.java
Song.java
Sound.java
SoundBank.java
SoundPcmStream.java
SoundPlayer.java
SynthEnvelope.java
SynthFilter.java
SynthInstrument.java
SynthSound.java
VorbisCodebook.java
VorbisFloor.java
VorbisMapping.java
VorbisResidue.java
VorbisSound.java
```

---

## 2. rt4.commerce (Trading & Stock Market) - 2 files

**Package:** `rt4.world`

```
AbstractWorld.java
CollisionMap.java
Map.java
MapChunk.java
MapElement.java
MapList.java
MapMarker.java
PathFinder.java
World.java
WorldInfo.java
WorldList.java
```

---

## 3. rt4.core (Core Engine & Game Loop) - 15 files

**Package:** `rt4.core`

```
Camera.java
client.java
DelayedStateChange.java
DisplayMode.java
Environment.java
GameCanvas.java
GameShell.java
GlobalConfig.java
GlobalJsonConfig.java
HookRequest.java
Preferences.java
Protocol.java
ReflectionCheck.java
SignLinkAudioChannel.java
ThreadUtils.java
```

---

## 4. rt4.data (Game Data Types) - 45 files

**Package:** `rt4.data`

```
BasType.java
BasTypeList.java
EnumStringEntry.java
EnumType.java
EnumTypeList.java
Equipment.java
FloType.java
FloTypeList.java
FluType.java
FluTypeList.java
HitBarList.java
IdkType.java
IdkTypeList.java
Inv.java
InvType.java
InvTypeList.java
LightType.java
LightTypeList.java
LocType.java
LocTypeList.java
MapElementList.java
MsiType.java
MsiTypeList.java
NpcType.java
NpcTypeList.java
ObjType.java
ObjTypeList.java
ParamType.java
ParamTypeList.java
PlayerSkillXpTable.java
QuickChatCatType.java
QuickChatCatTypeList.java
QuickChatPhraseType.java
QuickChatPhraseTypeList.java
SeqType.java
SeqTypeList.java
SpotAnimType.java
SpotAnimTypeList.java
StructType.java
StructTypeList.java
VarbitType.java
VarbitTypeList.java
VarcDomain.java
VarpDomain.java
VarpType.java
VarpTypeList.java
```

---

## 5. rt4.model (3D Models & Animation) - 10 files

**Package:** `rt4.model`

```
AnimBase.java
AnimFrame.java
AnimFrameset.java
Model.java
RawModel.java
ShadowModelList.java
TriangleNormal.java
VertexNormal.java
```

---

## 6. rt4.network (Networking & I/O) - 24 files

**Package:** `rt4.network`

```
BrokenInputStream.java
BrokenOutputStream.java
Buffer.java
BufferedFile.java
BufferedSocket.java
Cache.java
ClientProt.java
Js5.java
Js5CachedResourceProvider.java
Js5CacheQueue.java
Js5CacheRequest.java
Js5Compression.java
Js5GlTextureProvider.java
Js5Index.java
Js5MasterIndex.java
Js5NetQueue.java
Js5NetRequest.java
Js5QuickChatCommandDecoder.java
Js5Request.java
Js5ResourceProvider.java
LoginManager.java
Packet.java
ServerActiveProperties.java
ServerProt.java
```

---

## 7. rt4.render (Main Rendering System) - 23 files

**Package:** `rt4.render`

```
FogManager.java
GlCleaner.java
GlRenderer.java
Light.java
Light_Class45.java
LightingManager.java
LiquidMaterialRenderer.java
MaterialManager.java
MaterialManager_Class106.java
MaterialManager_Class106_Sub1.java
MaterialManager_Class106_Sub1_Sub1.java
MaterialManager_Class106_Sub2.java
MaterialManager_Class106_Sub2_Sub1.java
MaterialRenderer.java
Rasteriser.java
SceneGraph.java
SceneGraph_Class120.java
ShadowManager.java
SpecularMaterialRenderer.java
UnderwaterMaterialRenderer.java
UnlitMaterialRenderer.java
WaterfallMaterialRenderer.java
WaterMaterialRenderer.java
```

---

## 8. rt4.render.primitive (Rendering Primitives) - 29 files

**Package:** `rt4.render.primitive`

```
BufferedImageFrameBuffer.java
FrameBuffer.java
GlAlphaSprite.java
GlBoundingBox.java
GlBuffer.java
GlFont.java
GlIndexedSprite.java
GlModel.java
GlModel_Class23.java
GlRaster.java
GlSolidColorTexture.java
GlSprite.java
GlTexture.java
GlTile.java
GlVertexBufferObject.java
ImageProducerFrameBuffer.java
IndexedSprite.java
ParticleNode.java
ParticleSystem.java
Shadow.java
SoftwareAlphaSprite.java
SoftwareFont.java
SoftwareIndexedSprite.java
SoftwareModel.java
SoftwareRaster.java
SoftwareSprite.java
Sprite.java
SpriteLoader.java
Sprites.java
```

---

## 9. rt4.render.texture (Texture System) - 48 files

**Package:** `rt4.render.texture`

```
Texture.java
TextureOp.java
TextureOp4.java
TextureOp5.java
TextureOp11.java
TextureOp12.java
TextureOp14.java
TextureOp15.java
TextureOp16.java
TextureOp17.java
TextureOp19.java
TextureOp23.java
TextureOp25.java
TextureOp27.java
TextureOp28.java
TextureOp29.java
TextureOp29SubOp.java
TextureOp29SubOp1.java
TextureOp29SubOp2.java
TextureOp29SubOp3.java
TextureOp29SubOp4.java
TextureOp31.java
TextureOp32.java
TextureOp33.java
TextureOp34.java
TextureOp35.java
TextureOp37.java
TextureOp38.java
TextureOpBinary.java
TextureOpClamp.java
TextureOpColorFill.java
TextureOpColorGradient.java
TextureOpCombine.java
TextureOpCurve.java
TextureOpFlip.java
TextureOpHorizontalGradient.java
TextureOpInterpolate.java
TextureOpInvert.java
TextureOpMonochrome.java
TextureOpMonochromeFill.java
TextureOpNoise.java
TextureOpRange.java
TextureOpSprite.java
TextureOpTexture.java
TextureOpTile.java
TextureOpTiledSprite.java
TextureOpVerticalGradient.java
TextureProvider.java
```

---

## 10. rt4.scene (World & Scene Objects) - 26 files

**Package:** `rt4.scene`

```
AttachLocRequest.java
ChangeLocRequest.java
Cross.java
Entity.java
GroundDecor.java
HintArrowManager.java
Loc.java
LocEntity.java
Npc.java
ObjStack.java
ObjStackEntity.java
ObjStackNode.java
PathingEntity.java
PathingEntity_Class147.java
PlainTile.java
Player.java
PlayerAppearance.java
ProjAnim.java
ProjAnimNode.java
Scenery.java
ShapedTile.java
SpotAnim.java
SpotAnimNode.java
Tile.java
Wall.java
WallDecor.java
```

---

## 11. rt4.script (Scripting System) - 7 files

**Package:** `rt4.script`

```
ClientScript.java
ClientScriptList.java
Cs1ScriptRunner.java
GoSubFrame.java
QuickChatCommandDecoder.java
QuickChatPhrase.java
ScriptRunner.java
```

---

## 12. rt4.social (Social Features) - 9 files

**Package:** `rt4.social`

```
Chat.java
ClanChat.java
ClanMember.java
FriendsList.java
IgnoreList.java
LocalizedText.java
NpcList.java
OverheadChat.java
PlayerList.java
```

---

## 13. rt4.ui (User Interface) - 23 files

**Package:** `rt4.ui`

```
BrowserControl.java
Component.java
ComponentPointer.java
CreateManager.java
CursorType.java
CursorTypeList.java
Flames.java
Font.java
FontMetricsList.java
Fonts.java
InterfaceList.java
JavaMouseWheel.java
Keyboard.java
LoadingBar.java
LoadingBarAwt.java
MiniMap.java
MiniMenu.java
Mouse.java
MouseRecorder.java
MouseWheel.java
TitleScreen.java
WorldMap.java
WorldMapFont.java
```

---

## 14. rt4.util (Utilities & Data Structures) - 52 files

**Package:** `rt4.util`

```
ArrayUtils.java
Base37.java
Bzip2Decompressor.java
Bzip2DState.java
ByteArray.java
ByteArrayNode.java
ByteArrayNodeSecondary.java
CharUtils.java
Cheat.java
ColorImageCache.java
ColorImageCacheEntry.java
ColorUtils.java
DateUtil.java
DeadClass.java
DirectByteArray.java
Find.java
GzipDecompressor.java
HardReferenceNode.java
HashTable.java
HashTableIterator.java
HuffmanCodec.java
IntHashTable.java
IntNode.java
IntUtils.java
IsaacRandom.java
JagString.java
LangUtils.java
LinkedList.java
LongNode.java
LruHashTable.java
MathUtils.java
MillisTimer.java
MonochromeImageCache.java
MonochromeImageCacheEntry.java
NanoTimer.java
Node.java
PerlinNoise.java
RandomUtils.java
ReferenceNode.java
ReferenceNodeFactory.java
SecondaryHashTable.java
SecondaryLinkedList.java
SecondaryNode.java
SoftLruHashTable.java
SoftReferenceNode.java
SoftReferenceNodeFactory.java
StringInterface.java
StringNode.java
StringUtils.java
Timer.java
TracingException.java
WordPack.java
```

---

## 15. rt4.world (World & Map Data) - 11 files

**Package:** `rt4.ui`

```
AbstractWorld.java
CollisionMap.java
Map.java
MapChunk.java
MapElement.java
MapList.java
MapMarker.java
PathFinder.java
World.java
WorldInfo.java
WorldList.java
```

---

## Summary

- **Total files to move:** ~350 files
- **New packages:** 15 packages
- **Files staying in rt4:** gl/ folder (already organized), amilious/ folder (custom code)

## Recommended Execution Order

1. **util** (no dependencies)
2. **data** (game data types)
3. **model**, **render.texture**
4. **render.primitive**, **render**
5. **scene**, **audio**, **network**
6. **ui**, **social**, **world**, **script**
7. **core**, **commerce**

## After Each Package Move

Quick compile check: `./gradlew :client:compileJava`

## After All Refactoring

1. Full build: `./gradlew :client:build -x test`
2. Commit changes
3. Update GRAPHICS_BACKEND_PLAN.md if needed
