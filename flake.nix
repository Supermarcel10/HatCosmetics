{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = { nixpkgs, ... }: let
    forAllSystems = nixpkgs.lib.genAttrs nixpkgs.lib.platforms.unix;
  in {
    devShells = forAllSystems (system: let
      pkgs = nixpkgs.legacyPackages.${system};
      jdk = pkgs.jdk21;
    in {
      default = pkgs.mkShell {
        packages = [
          jdk
          pkgs.gradle
        ];

        JAVA_HOME = jdk.home;
        shellHook = ''
          echo "  JDK:   $JAVA_HOME"
          echo "  Java:  $(java -version 2>&1 | head -1)"
          echo "  Gradle: $(gradle --version 2>&1 | grep 'Gradle ')"
        '';
      };
    });
  };
}
