//
// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//  ShaderPlain.vsh
//

attribute highp vec3    myVertex;
attribute mediump vec2  myUV;
varying mediump vec2    texCoord;
uniform highp mat4      uPMatrix;

void main(void)
{
    highp vec4 p = vec4(myVertex,1);
    gl_Position = uPMatrix * p;

    texCoord = myUV;
}
