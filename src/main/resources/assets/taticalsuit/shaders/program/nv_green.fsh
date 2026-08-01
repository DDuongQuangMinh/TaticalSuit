/* STREAMING_CHUNK: Setting up shader variables and random noise function */
#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform vec2 OutSize;
uniform float Time;

out vec4 fragColor;

float random(vec2 st) {
    return fract(sin(dot(st, vec2(12.9898, 78.233))) * 43758.5453123);
}

void main() {
    /* STREAMING_CHUNK: Fetching base colors and calculating luminance */
    // Note: texture2D changed to texture for #version 150 compatibility
    vec4 color = texture(DiffuseSampler, texCoord);

    // intensifier: lift shadow detail, then apply overall gain with increased contrast
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    luminance = pow(luminance, 0.25);  // steeper curve for more contrast
    luminance *= 0.8;  // further reduce overall brightness

    /* STREAMING_CHUNK: Applying green phosphor tint, grain, bloom, and vignette */
    // phosphor tint: darker forest green
    vec3 nv = luminance * vec3(0.25, 0.5, 0.08);

    // grain, re-rolled every frame via Time
    float grain = random(texCoord * OutSize + Time * 60.0) * 0.25;
    
    // add finer grain for more noise texture
    float fineGrain = random(texCoord * OutSize * 2.0 + Time * 45.0) * 0.15;
    grain += fineGrain;
    
    // scan lines for classic night vision look
    float scanLines = sin(texCoord.y * OutSize.y * 2.0) * 0.06;
    
    // apply all noise effects
    nv += vec3(grain + scanLines);

    // bloom: bright sources smear toward white-green
    float bloom = smoothstep(0.75, 1.3, luminance);
    nv = mix(nv, vec3(0.85, 1.0, 0.85), bloom * 0.6);
    
    // overexposure/washout for very bright light sources (daylight, torches, lava)
    // this simulates the night vision being blinded by bright lights
    float overexposure = smoothstep(0.4, 0.9, luminance);
    nv = mix(nv, vec3(1.0, 1.0, 0.95), overexposure * 1.2);

    // dual overlapping lens vignette (binocular goggles effect)
    vec2 screenPos = (texCoord - 0.5) * vec2(OutSize.x / OutSize.y, 1.0);
    
    // Create two circular lenses that overlap in the center with better blend
    float leftLensDist = length(screenPos - vec2(-0.22, 0.0));
    float rightLensDist = length(screenPos - vec2(0.22, 0.0));
    
    // Combine the two lenses with tighter falloff for darker borders
    float leftVig = 1.0 - smoothstep(0.50, 0.65, leftLensDist);
    float rightVig = 1.0 - smoothstep(0.50, 0.65, rightLensDist);
    float vig = max(leftVig, rightVig);
    
    nv *= vig;

    /* STREAMING_CHUNK: Outputting final compiled color */
    fragColor = vec4(nv, color.a);
}